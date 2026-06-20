package com.example.game

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class BikeState(
    val x: Float = 60f,
    val y: Float = 300f,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val angle: Float = 0f, // radians
    val angularVelocity: Float = 0f,
    val isGrounded: Boolean = false,
    val isCrashed: Boolean = false,
    val consecutiveFlipsCount: Int = 0,
    val currentAirRotationDegrees: Float = 0f,
    val distanceCompleted: Float = 0f,
    val flipBonusAwarded: Int = 0 // keeps track of how many bonus coins we earned in this session
)

object BikePhysics {

    /**
     * Updates physics for a single frame step.
     */
    fun step(
        currentState: BikeState,
        throttle: Boolean,
        brake: Boolean,
        tiltForward: Boolean,
        tiltBack: Boolean,
        engineLevel: Int,
        suspensionLevel: Int,
        tiresLevel: Int,
        weightLevel: Int,
        track: TrackInfo
    ): BikeState {
        if (currentState.isCrashed) return currentState

        var x = currentState.x
        var y = currentState.y
        var vx = currentState.vx
        var vy = currentState.vy
        var angle = currentState.angle
        var angularVelocity = currentState.angularVelocity
        var isGrounded = currentState.isGrounded
        var currentAirRotationDegrees = currentState.currentAirRotationDegrees
        var consecutiveFlipsCount = currentState.consecutiveFlipsCount
        var flipBonusAwarded = currentState.flipBonusAwarded

        // 1. Get track surface height and slope
        val groundY = TrackData.getTrackHeight(track.id, x)
        val groundSlope = TrackData.getTrackSlope(track.id, x)

        // 2. Compute upgrade parameters
        val maxSpeed = 14f + (engineLevel - 1) * 2.2f
        val accelPower = 0.16f + (engineLevel - 1) * 0.04f
        val gripFactor = (1.0f + (tiresLevel - 1) * 0.18f) / track.slideMultiplier
        val weightGravityMultiplier = 1.0f - (weightLevel - 1) * 0.05f
        val airRotationSpeed = 0.045f + (weightLevel - 1) * 0.01f
        // Standard crash angle boundary deviation from slope is 1.1 radians (~63 degrees).
        // Max suspension levels increase this to ~1.5 radians (~86 degrees).
        val crashAngleBound = 1.05f + (suspensionLevel - 1) * 0.11f

        // 3. Apply gravity
        val gravity = track.baseGravity * weightGravityMultiplier
        vy += gravity

        // Air/drag resistances
        vx *= 0.994f
        vy *= 0.994f

        val wheelsYOffset = 18f
        val collisionThresholdY = groundY - wheelsYOffset

        // Check if we hit lava (hardcoded lava bounds in Volcanic track)
        val isLavaCrash = track.id == "volcano" && y > 1050f && (
            x in 450f..530f || x in 1150f..1240f || x in 1850f..1950f
        )
        if (isLavaCrash) {
            return currentState.copy(isCrashed = true, isGrounded = false)
        }

        if (y >= collisionThresholdY) {
            // Contact with track surface!
            y = collisionThresholdY

            if (!isGrounded) {
                // Landing verification
                val angleDiff = abs(normalizeAngle(angle - groundSlope))
                if (angleDiff > crashAngleBound) {
                    // Crashed on landing!
                    return currentState.copy(isCrashed = true, isGrounded = false)
                }
                
                // Landed safely
                isGrounded = true
                // Suspension dampening on impact
                vy = vy * (0.35f - (suspensionLevel - 1) * 0.05f)
                if (vy < 0f) vy = 0f
                
                // Flip detection check on landing
                val flips = (abs(currentAirRotationDegrees) / 320f).toInt()
                if (flips > 0) {
                    consecutiveFlipsCount += flips
                    flipBonusAwarded += flips
                }
                currentAirRotationDegrees = 0f
            }

            // Conform bike angle to the terrain
            val targetAngle = groundSlope
            // Soft interpolation of angle to ride smoothly over hills
            angle = normalizeAngle(angle * 0.72f + targetAngle * 0.28f)
            angularVelocity = 0f

            // Throttle & Brake behavior
            val rollingFriction = 0.04f / gripFactor
            if (throttle) {
                val thrustX = cos(groundSlope) * accelPower
                val thrustY = sin(groundSlope) * accelPower
                vx += thrustX.toFloat()
                vy += thrustY.toFloat()
            }
            if (brake) {
                // Reversing or braking
                val brakeX = -cos(groundSlope) * accelPower * 0.8f
                val brakeY = -sin(groundSlope) * accelPower * 0.8f
                vx += brakeX.toFloat()
                vy += brakeY.toFloat()
            }

            // Passive rolling resistance
            vx *= (1.0f - rollingFriction * 0.1f)

        } else {
            // Mid-air flight
            isGrounded = false

            // Air manual rotation
            if (tiltBack) {
                angularVelocity += airRotationSpeed * 0.35f
            }
            if (tiltForward) {
                angularVelocity -= airRotationSpeed * 0.35f
            }

            angularVelocity = maxOf(-0.16f, minOf(0.16f, angularVelocity))
            angle = normalizeAngle(angle + angularVelocity)
            
            // Air friction slowing rotation
            angularVelocity *= 0.95f

            // Air Rotation tracking for flip counts
            currentAirRotationDegrees += Math.toDegrees(angularVelocity.toDouble()).toFloat()
        }

        // Max velocity constraint
        vx = maxOf(-4f, minOf(maxSpeed, vx))
        x += vx
        y += vy

        // Restrain bounds
        if (x < 10f) {
            x = 10f
            vx = 0f
        }

        return BikeState(
            x = x,
            y = y,
            vx = vx,
            vy = vy,
            angle = angle,
            angularVelocity = angularVelocity,
            isGrounded = isGrounded,
            isCrashed = false,
            consecutiveFlipsCount = consecutiveFlipsCount,
            currentAirRotationDegrees = currentAirRotationDegrees,
            distanceCompleted = x,
            flipBonusAwarded = flipBonusAwarded
        )
    }

    /**
     * Normalizes an angle in radians to the range [-PI, PI].
     */
    fun normalizeAngle(angle: Float): Float {
        var a = angle
        while (a > PI) a -= (2.0f * PI.toFloat())
        while (a < -PI) a += (2.0f * PI.toFloat())
        return a
    }
}
