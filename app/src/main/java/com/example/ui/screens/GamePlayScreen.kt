package com.example.ui.screens

import android.view.MotionEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.TrackData
import com.example.game.TrackInfo
import com.example.ui.GameViewModel
import com.example.ui.PlayState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val bikeState by viewModel.bikeState.collectAsState()
    val playState by viewModel.gameState.collectAsState()
    val countdownVal by viewModel.countdownVal.collectAsState()
    val raceTimeSec by viewModel.raceTimeMillis.collectAsState()
    val coinsInSession by viewModel.coinsInSession.collectAsState()
    val flipsInSession by viewModel.flipsInSession.collectAsState()
    val activeTrack by viewModel.activeTrack.collectAsState()
    val activeBike by viewModel.activeBike.collectAsState()
    val coinsOnTrack by viewModel.coinsOnTrack.collectAsState()
    val boostPads by viewModel.boostPads.collectAsState()
    val activeDecos by viewModel.activeDecos.collectAsState()
    val trickNotification by viewModel.trickNotification.collectAsState()
    val isBoosting by viewModel.isBoosting.collectAsState()

    val colors = MaterialTheme.colorScheme
    val track = activeTrack ?: TrackData.TRACKS[0]
    val bikeColor = Color(android.graphics.Color.parseColor(activeBike?.primaryColorHex ?: "#E11D48"))
    val trimColor = Color(android.graphics.Color.parseColor(activeBike?.secondaryColorHex ?: "#1E293B"))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // High-performance Canvas Racing Window
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("racing_canvas")
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Drawing parallax sky backdrop
            drawRect(
                brush = Brush.verticalGradient(
                    colors = track.skyColors
                ),
                topLeft = Offset(0f, 0f),
                size = size
            )

            // Calculate scrolling viewport camera centers
            val cameraX = maxOf(0f, bikeState.x - (canvasW * 0.28f))
            
            // Limit vertical scope so we don't drop out of screen view
            val baseTrackY = TrackData.getTrackHeight(track.id, bikeState.x)
            val cameraY = baseTrackY - (canvasH * 0.62f)

            // Draw distant neon mountains/canyons background decoration
            val hillColor1 = track.obstacleColor.copy(alpha = 0.45f)
            val pathHill1 = Path().apply {
                moveTo(0f, canvasH)
                for (x in 0..canvasW.toInt() step 50) {
                    val worldX = cameraX * 0.25f + x // parallax
                    val hy = 580f + sin(worldX * 0.002f) * 120f + cos(worldX * 0.005f) * 40f
                    lineTo(x.toFloat(), hy - cameraY * 0.3f)
                }
                lineTo(canvasW, canvasH)
                close()
            }
            drawPath(pathHill1, hillColor1)

            // Draw closer mountains parallax layer
            val hillColor2 = track.obstacleColor.copy(alpha = 0.72f)
            val pathHill2 = Path().apply {
                moveTo(0f, canvasH)
                for (x in 0..canvasW.toInt() step 40) {
                    val worldX = cameraX * 0.5f + x // parallax
                    val hy = 620f + sin(worldX * 0.004f) * 110f + sin(worldX * 0.012f) * 35f
                    lineTo(x.toFloat(), hy - cameraY * 0.6f)
                }
                lineTo(canvasW, canvasH)
                close()
            }
            drawPath(pathHill2, hillColor2)

            // Draw landscape decos (cacti, neon poles, etc)
            activeDecos.forEach { deco ->
                if (deco.x >= cameraX - 120f && deco.x <= cameraX + canvasW + 120f) {
                    val sx = deco.x - cameraX
                    val dy = TrackData.getTrackHeight(track.id, deco.x) - cameraY
                    
                    when (deco.type) {
                        "cactus" -> {
                            // Draw nice green saguaro cactus vector style
                            val h = 65f * deco.scale
                            drawRect(
                                color = track.obstacleColor,
                                topLeft = Offset(sx - 4f, dy - h),
                                size = Size(8f, h)
                            )
                            // Left wing
                            drawLine(
                                color = track.obstacleColor,
                                start = Offset(sx - 16f, dy - h * 0.5f),
                                end = Offset(sx, dy - h * 0.5f),
                                strokeWidth = 6f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = track.obstacleColor,
                                start = Offset(sx - 16f, dy - h * 0.5f),
                                end = Offset(sx - 16f, dy - h * 0.8f),
                                strokeWidth = 6f,
                                cap = StrokeCap.Round
                            )
                            // Right wing
                            drawLine(
                                color = track.obstacleColor,
                                start = Offset(sx, dy - h * 0.7f),
                                end = Offset(sx + 14f, dy - h * 0.7f),
                                strokeWidth = 6f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = track.obstacleColor,
                                start = Offset(sx + 14f, dy - h * 0.7f),
                                end = Offset(sx + 14f, dy - h * 0.95f),
                                strokeWidth = 6f,
                                cap = StrokeCap.Round
                            )
                        }
                        "neon_tower" -> {
                            val h = 100f * deco.scale
                            // Draw nice glowing neon beacon posts
                            drawLine(
                                color = Color(0xFF475569),
                                start = Offset(sx, dy),
                                end = Offset(sx, dy - h),
                                strokeWidth = 3f
                            )
                            drawCircle(
                                color = track.accentColor,
                                radius = 7f,
                                center = Offset(sx, dy - h)
                            )
                            drawCircle(
                                color = track.accentColor.copy(alpha = 0.3f),
                                radius = 22f,
                                center = Offset(sx, dy - h)
                            )
                        }
                        "pine_tree" -> {
                            val h = 90f * deco.scale
                            val tPath = Path().apply {
                                moveTo(sx, dy - h)
                                lineTo(sx - 18f * deco.scale, dy - h * 0.4f)
                                lineTo(sx - 8f * deco.scale, dy - h * 0.4f)
                                lineTo(sx - 24f * deco.scale, dy)
                                lineTo(sx + 24f * deco.scale, dy)
                                lineTo(sx + 8f * deco.scale, dy - h * 0.4f)
                                lineTo(sx + 18f * deco.scale, dy - h * 0.4f)
                                close()
                            }
                            drawPath(tPath, track.obstacleColor)
                        }
                        "lava_geyser" -> {
                            val h = 40f * deco.scale
                            // Erupting magma vent shape
                            val pathMag = Path().apply {
                                moveTo(sx - 20f, dy)
                                lineTo(sx - 8f, dy - h)
                                lineTo(sx + 8f, dy - h)
                                lineTo(sx + 20f, dy)
                                close()
                            }
                            drawPath(pathMag, Color(0xFF1C0A00))
                            drawCircle(
                                color = track.accentColor,
                                radius = 12f * deco.scale,
                                center = Offset(sx, dy - h - 5f)
                            )
                        }
                    }
                }
            }

            // Draw checkered target goal pole
            val poleX = track.length - cameraX
            val poleGroundY = TrackData.getTrackHeight(track.id, track.length) - cameraY
            if (track.length >= cameraX && track.length <= cameraX + canvasW) {
                // Goal posts
                drawLine(
                    color = Color.White,
                    start = Offset(poleX, poleGroundY),
                    end = Offset(poleX, poleGroundY - 240f),
                    strokeWidth = 10f
                )
                // Draw Checkerboard checkered banner cloth
                for (r in 0..4) {
                    for (c in 0..3) {
                        val tileX = poleX + c * 15f
                        val tileY = poleGroundY - 240f + r * 15f
                        val tileColor = if ((r + c) % 2 == 0) Color.White else Color.Black
                        drawRect(
                            color = tileColor,
                            topLeft = Offset(tileX, tileY),
                            size = Size(15f, 15f)
                        )
                    }
                }
            }

            // Draw Boost pads
            boostPads.forEach { pad ->
                if (pad.x >= cameraX - 50f && pad.x <= cameraX + canvasW) {
                    val sx = pad.x - cameraX
                    val sy = TrackData.getTrackHeight(track.id, pad.x) - cameraY
                    
                    // Neon electric arrow boost plate
                    drawRect(
                        color = track.accentColor.copy(alpha = 0.4f),
                        topLeft = Offset(sx, sy - 4f),
                        size = Size(pad.width, 10f)
                    )
                    // Pulse arrows
                    for (i in 0..2) {
                        val arrowOffset = sx + i * 14f + 4f
                        val arrowPath = Path().apply {
                            moveTo(arrowOffset, sy - 2f)
                            lineTo(arrowOffset + 8f, sy - 8f)
                            lineTo(arrowOffset, sy - 14f)
                        }
                        drawPath(
                            path = arrowPath,
                            color = Color.White,
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // Draw Soil / Solid hill terrain path underneath the bike
            val groundPath = Path().apply {
                moveTo(0f, canvasH)
                // Step values loop across horizontally visible coordinate frames
                for (px in 0..canvasW.toInt() step 12) {
                    val worldX = cameraX + px
                    val gy = TrackData.getTrackHeight(track.id, worldX.toFloat())
                    lineTo(px.toFloat(), gy - cameraY)
                }
                lineTo(canvasW, canvasH)
                close()
            }
            // Fill core land mass
            drawPath(
                path = groundPath,
                color = track.groundColor
            )

            // Draw gorgeous neon border accent line along track path
            val borderPath = Path().apply {
                var first = true
                for (px in 0..canvasW.toInt() step 12) {
                    val worldX = cameraX + px
                    val gy = TrackData.getTrackHeight(track.id, worldX.toFloat())
                    val sy = gy - cameraY
                    if (first) {
                        moveTo(px.toFloat(), sy)
                        first = false
                    } else {
                        lineTo(px.toFloat(), sy)
                    }
                }
            }
            drawPath(
                path = borderPath,
                color = track.pathStrokeColor,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )

            // Draw Collectible Coins
            coinsOnTrack.forEach { coin ->
                if (!coin.isCollected && coin.x >= cameraX - 30f && coin.x <= cameraX + canvasW + 30f) {
                    val sx = coin.x - cameraX
                    val sy = coin.y - cameraY

                    // Outward spin pulse
                    drawCircle(
                        color = Color(0xFFFBBF24).copy(alpha = 0.25f),
                        radius = 16f,
                        center = Offset(sx, sy)
                    )
                    // Inner filled coin
                    drawCircle(
                        color = Color(0xFFFBBF24),
                        radius = 10f,
                        center = Offset(sx, sy)
                    )
                    // Gold outer ring border sparkle
                    drawCircle(
                        color = Color.White,
                        radius = 10f,
                        center = Offset(sx, sy),
                        style = Stroke(width = 2f)
                    )
                }
            }

            // 2. DRAW THE PLAYER'S PERFORMANCE MOTORCYCLE
            val bx = bikeState.x - cameraX
            val by = bikeState.y - cameraY

            // Rotated context container matching bike angle
            withTransform({
                val bikeDeg = Math.toDegrees(bikeState.angle.toDouble()).toFloat()
                rotate(degrees = bikeDeg, pivot = Offset(bx, by))
            }) {
                // Rocket exhaust flares when throttle holds!
                if (playState == PlayState.RUNNING && (viewModel.isThrottlePressed || isBoosting)) {
                    val flareW = if (isBoosting) 80f else 46f
                    val flarePath = Path().apply {
                        moveTo(bx - 32f, by + 2f)
                        lineTo(bx - 32f - flareW, by - 6f)
                        lineTo(bx - 32f, by - 12f)
                        close()
                    }
                    drawPath(
                        path = flarePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(track.accentColor, Color.Yellow.copy(alpha = 0.1f)),
                            startX = bx - 30f,
                            endX = bx - 30f - flareW
                        )
                    )
                }

                // Cyber wheels spin based on position progress x!
                val rotationAngle = (bikeState.x / 14f) % (2f * PI.toFloat())

                // Rear chassis wheel
                drawGameWheel(
                    cx = bx - 21f,
                    cy = by + 12f,
                    radius = 15f,
                    rotationRad = rotationAngle,
                    colorRim = Color.Black,
                    colorInner = Color.White,
                    colorSpoke = track.pathStrokeColor
                )

                // Front chassis wheel
                drawGameWheel(
                    cx = bx + 21f,
                    cy = by + 12f,
                    radius = 15f,
                    rotationRad = rotationAngle,
                    colorRim = Color.Black,
                    colorInner = Color.White,
                    colorSpoke = track.pathStrokeColor
                )

                // Sleek Chassis forks & shock joints
                drawLine(
                    color = Color(0xFF94A3B8), // chrome fork
                    start = Offset(bx - 21f, by + 12f),
                    end = Offset(bx - 4f, by - 4f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF94A3B8), // front fork
                    start = Offset(bx + 21f, by + 12f),
                    end = Offset(bx + 8f, by - 8f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )

                // Custom painted engine shell card frame
                val bodyPath = Path().apply {
                    moveTo(bx - 14f, by + 2f)
                    lineTo(bx - 24f, by - 6f)
                    lineTo(bx - 16f, by - 16f)
                    lineTo(bx + 11f, by - 16f)
                    lineTo(bx + 15f, by - 4f)
                    lineTo(bx + 5f, by + 6f)
                    close()
                }
                drawPath(path = bodyPath, color = trimColor)

                val primaryFairingPath = Path().apply {
                    moveTo(bx - 8f, by - 4f)
                    lineTo(bx - 14f, by - 15f)
                    lineTo(bx + 8f, by - 15f)
                    lineTo(bx + 12f, by - 5f)
                    close()
                }
                drawPath(path = primaryFairingPath, color = bikeColor)

                // Handlebars vector
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(bx + 8f, by - 8f),
                    end = Offset(bx + 6f, by - 24f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.Black,
                    start = Offset(bx + 6f, by - 24f),
                    end = Offset(bx + 1f, by - 25f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )

                // Rider helmet avatar bubble
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(bx - 4f, by - 26f)
                )
                drawArc(
                    color = Color.Black,
                    startAngle = -45f,
                    sweepAngle = 100f,
                    useCenter = true,
                    topLeft = Offset(bx - 12f, by - 34f),
                    size = Size(16f, 16f)
                ) // sleek visor
            }
        }

        // HUD Top Metrics Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Return / Pause
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo("TrackSelect") },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B).copy(alpha = 0.72f))
                            .size(40.dp)
                            .testTag("game_back_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (playState == PlayState.RUNNING) {
                        IconButton(
                            onClick = { viewModel.pauseGame() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.surface.copy(alpha = 0.85f))
                                .size(40.dp)
                                .testTag("game_pause_btn")
                        ) {
                            // High contrast vector-drawn pause graphic
                            Row(
                                modifier = Modifier.size(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(colors.onSurface))
                                Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(colors.onSurface))
                            }
                        }
                    } else if (playState == PlayState.PAUSED) {
                        IconButton(
                            onClick = { viewModel.resumeGame() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.85f))
                                .size(40.dp)
                                .testTag("game_resume_btn")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume", tint = colors.onPrimary)
                        }
                    }
                }

                // Balance Coins & Tricks Displays
                Row {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .border(1.dp, colors.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪙 $coinsInSession", color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, colors.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⏱️ ${formatTime(raceTimeSec)}", color = colors.onSurface, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Slider Indicator
            val progressFactor = bikeState.x / track.length
            val visibleProgress = progressFactor.coerceIn(0f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(colors.secondary.copy(alpha = 0.5f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(visibleProgress)
                        .background(track.pathStrokeColor)
                )
            }
        }

        // Large Mid-screen Countdown Animation
        if (playState == PlayState.COUNTDOWN) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$countdownVal",
                    color = Color.White,
                    fontSize = 90.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Mid-air flip stunt banner overlay notifications!
        AnimatedVisibility(
            visible = trickNotification != null,
            enter = fadeIn(animationSpec = spring()) + expandVertically(),
            exit = fadeOut(animationSpec = spring()) + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 135.dp)
        ) {
            Text(
                text = trickNotification ?: "",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color(0xFFFF007F).copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // SPEEDOMETER overlay gauge
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 125.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val speedDisplay = (bikeState.vx * 1.5f * 10f).toInt().coerceAtLeast(0)
            Text(
                text = "$speedDisplay",
                color = if (isBoosting) Color(0xFF00FFCC) else Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = if (isBoosting) "HYPER BOOST ACTIVE!" else "KM/H",
                color = if (isBoosting) Color(0xFF00FFCC) else Color(0xFF64748B),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Touch Control Pedals (Accessible Touch bounds of 48.dp)
        // Left hand side: TILT FORWARD / TILT BACK (air stunts)
        // Right hand side: BRAKE / GAS throttle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Levers (Air tilt rotation controls)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Tilt Back Wheelie controller
                TouchLeverPad(
                    arrowSymbol = "↩️",
                    label = "TILT L",
                    tagId = "tilt_left_lever",
                    onTouchChanged = { pressed -> viewModel.isTiltForwardPressed = pressed }
                )

                // Tilt Forward controller
                TouchLeverPad(
                    arrowSymbol = "↪️",
                    label = "TILT R",
                    tagId = "tilt_right_lever",
                    onTouchChanged = { pressed -> viewModel.isTiltBackPressed = pressed }
                )
            }

            // Pedals (Ground propulsion drive controls)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Brake pedal
                TouchPedalPad(
                    label = "BRAKE",
                    color = Color(0xFFEF4444),
                    tagId = "brake_pedal_btn",
                    onTouchChanged = { pressed -> viewModel.isBrakePressed = pressed }
                )

                // Throttle pedal
                TouchPedalPad(
                    label = "GAS",
                    color = Color(0xFF10B981),
                    tagId = "gas_pedal_btn",
                    onTouchChanged = { pressed -> viewModel.isThrottlePressed = pressed }
                )
            }
        }

        // Overlay states: CRASHED or VICTORY dialog frames!
        if (playState == PlayState.CRASHED) {
            GameOverModal(
                title = "MOTORCYCLE CRASHED!",
                titleColor = Color(0xFFE57373),
                subtitle = "Align bike tires flush with the landscape slope to land safely!",
                coinsEarned = coinsInSession,
                flipsAchieved = flipsInSession,
                onRestart = { viewModel.restartGame() },
                onExit = { viewModel.navigateTo("TrackSelect") }
            )
        } else if (playState == PlayState.VICTORY) {
            GameOverModal(
                title = "🏆 RIVALRY CONQUERED!",
                titleColor = colors.primary,
                subtitle = "Flat track completion reward (+100🪙) saved!",
                coinsEarned = coinsInSession + 100,
                flipsAchieved = flipsInSession,
                recordTimeSec = raceTimeSec,
                onRestart = { viewModel.restartGame() },
                onExit = { viewModel.navigateTo("TrackSelect") }
            )
        } else if (playState == PlayState.PAUSED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    modifier = Modifier.border(1.dp, colors.outline, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SIMULATION BREAK",
                            color = colors.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { viewModel.resumeGame() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("RESUME RACE", fontWeight = FontWeight.Bold, color = colors.onPrimary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.navigateTo("TrackSelect") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.outline),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("ABANDON RUN", fontWeight = FontWeight.Bold, color = colors.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TouchLeverPad(
    arrowSymbol: String,
    label: String,
    tagId: String,
    onTouchChanged: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(
                if (isPressed) colors.primary.copy(alpha = 0.25f) else colors.surface.copy(alpha = 0.85f)
            )
            .border(1.dp, if (isPressed) colors.primary else colors.outline, CircleShape)
            .testTag(tagId)
            .pointerInputTouchHold { pressed ->
                isPressed = pressed
                onTouchChanged(pressed)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = arrowSymbol, color = colors.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = colors.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TouchPedalPad(
    label: String,
    color: Color,
    tagId: String,
    onTouchChanged: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .width(68.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPressed) color.copy(alpha = 0.25f) else colors.surface.copy(alpha = 0.85f)
            )
            .border(1.dp, if (isPressed) color else colors.outline, RoundedCornerShape(12.dp))
            .testTag(tagId)
            .pointerInputTouchHold { pressed ->
                isPressed = pressed
                onTouchChanged(pressed)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isPressed) colors.onSurface else colors.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.sp
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.pointerInputTouchHold(onHoldChanged: (Boolean) -> Unit): Modifier = this.pointerInteropFilter { motionEvent ->
    when (motionEvent.action) {
        MotionEvent.ACTION_DOWN -> {
            onHoldChanged(true)
            true
        }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            onHoldChanged(false)
            true
        }
        else -> false
    }
}

@Composable
fun GameOverModal(
    title: String,
    titleColor: Color,
    subtitle: String,
    coinsEarned: Int,
    flipsAchieved: Int,
    recordTimeSec: Long? = null,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, titleColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Race Result box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.background)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("MINTED GOLD", color = colors.onSurfaceVariant.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("+ $coinsEarned 🪙", color = colors.primary, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("STUNT AIRFLIPS", color = colors.onSurfaceVariant.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("$flipsAchieved flips", color = colors.onSurface, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }

                    if (recordTimeSec != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("COMPLETION TIME", color = colors.onSurfaceVariant.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(formatTime(recordTimeSec), color = colors.primary, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("game_over_restart_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = colors.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RUN AGAIN", fontWeight = FontWeight.Bold, color = colors.onPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("game_over_exit_btn"),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.outline),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("BACK TO WORLD", fontWeight = FontWeight.Bold, color = colors.onSurface)
                }
            }
        }
    }
}

/**
 * Draws a rotating 2D bicycle wheel with glowing neon spokes.
 */
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGameWheel(
    cx: Float,
    cy: Float,
    radius: Float,
    rotationRad: Float,
    colorRim: Color,
    colorInner: Color,
    colorSpoke: Color
) {
    // 1. Draw outer tire rubber
    drawCircle(
        color = Color(0xFF090D1A), // deep rubber black
        radius = radius + 3f,
        center = Offset(cx, cy)
    )

    // 2. Draw metallic outline rim
    drawCircle(
        color = colorSpoke,
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 3.5f)
    )

    // 3. Draw rotating spokes
    val spokeCount = 6
    for (i in 0 until spokeCount) {
        val angle = rotationRad + (i * PI.toFloat() / (spokeCount / 2f))
        val endX = cx + cos(angle) * (radius - 2f)
        val endY = cy + sin(angle) * (radius - 2f)
        drawLine(
            color = colorSpoke.copy(alpha = 0.72f),
            start = Offset(cx, cy),
            end = Offset(endX, endY),
            strokeWidth = 2f
        )
    }

    // 4. Center hub
    drawCircle(
        color = Color.LightGray,
        radius = 3.5f,
        center = Offset(cx, cy)
    )
}
