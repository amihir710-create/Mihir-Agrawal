package com.example.game

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

data class GameCoin(
    val id: Int,
    val x: Float,
    val y: Float,
    var isCollected: Boolean = false
)

data class BoostPad(
    val x: Float,
    val width: Float = 40f
)

data class EnvironmentalDeco(
    val x: Float,
    val type: String, // "cactus", "neon_tower", "pine_tree", "lava_geyser"
    val scale: Float
)

data class TrackInfo(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: String, // "Easy", "Medium", "Hard", "Insane"
    val unlockCost: Int,
    val length: Float = 2500f, // 2.5 km
    val baseGravity: Float = 0.28f,
    val slideMultiplier: Float = 1.0f, // high sliding on snow
    val skyColors: List<Color>,
    val groundColor: Color,
    val pathStrokeColor: Color,
    val obstacleColor: Color,
    val accentColor: Color
)

object TrackData {
    val TRACKS = listOf(
        TrackInfo(
            id = "canyon",
            name = "Canyon Ridge",
            description = "Ride through deep sandstone ravines, giant jump ramps, and dust storm dunes.",
            difficulty = "Easy",
            unlockCost = 0,
            baseGravity = 0.28f,
            slideMultiplier = 1.0f,
            skyColors = listOf(Color(0xFFFE8C00), Color(0xFFF83600)), // Warm desert sunrise
            groundColor = Color(0xFFC2512C), // Terracotta
            pathStrokeColor = Color(0xFFFFCC33), // Golden pathway
            obstacleColor = Color(0xFF4A1A10),
            accentColor = Color(0xFFFF9500)
        ),
        TrackInfo(
            id = "tokyo",
            name = "Neo Tokyo Grid",
            description = "High-speed synthetic hover boards, electric booster pads, and vertical cyber wallrides.",
            difficulty = "Medium",
            unlockCost = 200,
            baseGravity = 0.26f, // slightly lower gravity
            slideMultiplier = 0.8f, // sticky
            skyColors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)), // Deep matrix cyan
            groundColor = Color(0xFF1E1E2F), // Slate tech
            pathStrokeColor = Color(0xFF00FFCC), // Glowing Cyan
            obstacleColor = Color(0xFF2D124D),
            accentColor = Color(0xFFFF007F) // Hot Pink
        ),
        TrackInfo(
            id = "arctic",
            name = "Arctic Peak",
            description = "Navigate extreme snowy heights, slippery ice bridges, and death-defying vertical drops.",
            difficulty = "Hard",
            unlockCost = 400,
            baseGravity = 0.30f,
            slideMultiplier = 1.8f, // highly slippery
            skyColors = listOf(Color(0xFF1A2980), Color(0xFF26D0CE)), // Polar aurora
            groundColor = Color(0xFFE6F3FF), // Snowy frost
            pathStrokeColor = Color(0xFF00FA9A), // Minty Aurora outline
            obstacleColor = Color(0xFF4F7A94),
            accentColor = Color(0xFF00E5FF)
        ),
        TrackInfo(
            id = "volcano",
            name = "Volcano Blast",
            description = "Erupting tectonic fissures. Missed jumps land you directly in terminal molten lava pits!",
            difficulty = "Insane",
            unlockCost = 800,
            baseGravity = 0.34f, // heavy gravitational volcanic pressure
            slideMultiplier = 1.1f,
            skyColors = listOf(Color(0xFF141E30), Color(0xFF243B55), Color(0xFFE52D27)), // Ash sky & flares
            groundColor = Color(0xFF1A0A00), // Obsidian crust
            pathStrokeColor = Color(0xFFFF3E00), // Molten Orange
            obstacleColor = Color(0xFF0D0500),
            accentColor = Color(0xFFFF2400)
        )
    )

    fun getTrackInfo(id: String): TrackInfo {
        return TRACKS.find { it.id == id } ?: TRACKS[0]
    }

    /**
     * Mathematical landscape function. Returns y-coordinate given x.
     * Note: In Android graphics space, larger Y means down.
     */
    fun getTrackHeight(trackId: String, x: Float): Float {
        if (x < 0f) return 550f
        return when (trackId) {
            "canyon" -> {
                // Gap 1: [550m, 620m]
                if (x in 550f..620f) {
                    850f + valScale(x, 550f, 620f) * 150f
                }
                // Gap 2: [1400m, 1480m]
                else if (x in 1400f..1480f) {
                    950f
                } 
                // Huge ramp: [1000m..1020m]
                else {
                    var h = 550f + 
                            sin(x * 0.005f) * 100f + 
                            cos(x * 0.015f) * 40f + 
                            sin(x * 0.035f) * 12f
                    // Custom ramp feature
                    if (x in 950f..1000f) {
                        val progress = (x - 950f) / 50f
                        h -= progress * 75f // Launch ramp!
                    }
                    if (x in 1800f..1900f) {
                        val progress = (x - 1800f) / 100f
                        h -= sin(progress * Math.PI.toFloat()) * 90f // Double summit
                    }
                    h
                }
            }
            "tokyo" -> {
                // Neon Grid: futuristic smooth plateaus and artificial curves
                if (x in 800f..880f) {
                    900f // Synthetic gap
                } else if (x in 1600f..1660f) {
                    950f
                } else {
                    var h = 500f + 
                            sin(x * 0.007f) * 60f + 
                            sin(x * 0.02f) * 15f
                    // Smooth futuristic launch deck
                    if (x in 400f..460f) {
                        val progress = (x - 400f) / 60f
                        h -= progress * 90f
                    }
                    // Cyber crest
                    if (x in 1200f..1300f) {
                        val progress = (x - 1200f) / 100f
                        h -= sin(progress * Math.PI.toFloat()) * 110f
                    }
                    h
                }
            }
            "arctic" -> {
                // Steep dangerous glaciers
                if (x in 700f..790f) {
                    1000f // Crevasse gap
                } else {
                    var h = 480f + 
                            sin(x * 0.004f) * 140f + 
                            cos(x * 0.018f) * 55f + 
                            sin(x * 0.04f) * 20f
                    // Ice slide jump
                    if (x in 1100f..1150f) {
                        val progress = (x - 1100f) / 50f
                        h -= progress * 100f
                    }
                    if (x in 1900f..2000f) {
                        val progress = (x - 1900f) / 100f
                        h += progress * 180f // Massive steep valley drop
                    }
                    h
                }
            }
            "volcano" -> {
                // Hard molten crusts and toxic chambers
                if (x in 450f..530f) {
                    1100f // Molten lava pit
                } else if (x in 1150f..1240f) {
                    1150f // Tectonic gap
                } else if (x in 1850f..1950f) {
                    1200f // Fire bowl
                } else {
                    var h = 600f + 
                            cos(x * 0.008f) * 160f + 
                            sin(x * 0.022f) * 45f + 
                            cos(x * 0.045f) * 15f
                    // Sudden volcano crater lip
                    if (x in 250f..300f) {
                        val progress = (x - 250f) / 50f
                        h -= progress * 120f
                    }
                    if (x in 1500f..1580f) {
                        val progress = (x - 1500f) / 80f
                        h -= sin(progress * Math.PI.toFloat()) * 130f
                    }
                    h
                }
            }
            else -> 550f
        }
    }

    private fun valScale(x: Float, min: Float, max: Float): Float {
        val mid = (min + max) / 2f
        val rad = (max - min) / 2f
        return 1.0f - (1.0f - cos((x - mid) / rad * Math.PI.toFloat() / 2f)).toFloat()
    }

    /**
     * Estimated slope angle (radians) of the ground path.
     */
    fun getTrackSlope(trackId: String, x: Float): Float {
        val dx = 1.0f
        val y1 = getTrackHeight(trackId, x - dx)
        val y2 = getTrackHeight(trackId, x + dx)
        return kotlin.math.atan2(y2 - y1, dx * 2f)
    }

    /**
     * Generates a repeatable set of coins for a given track.
     */
    fun generateCoinsForTrack(trackId: String, length: Float): List<GameCoin> {
        val list = mutableListOf<GameCoin>()
        var id = 0
        var x = 120f
        while (x < length - 100f) {
            val y = getTrackHeight(trackId, x)
            // Check if coin is over a deep gap - place them floating in mid-air to encourage jumps!
            val yGapCheck = getTrackHeight(trackId, x)
            if (yGapCheck >= 800f) {
                // Is a gap. Let's make an arch of coins floating in the sky!
                // Mid gap x is, say, x_mid
                // Find nearest gap range
                val (gapStart, gapEnd) = getGapBounds(trackId, x)
                if (gapStart > 0f) {
                    val gapWidth = gapEnd - gapStart
                    val progress = (x - gapStart) / gapWidth
                    // Floating arch path
                    val heightArch = sin(progress * Math.PI.toFloat()).toFloat() * 180f
                    val baseHeight = (getTrackHeight(trackId, gapStart) + getTrackHeight(trackId, gapEnd)) / 2f
                    list.add(GameCoin(id++, x, baseHeight - 60f - heightArch))
                }
            } else {
                // Regular coin: follow terrain curvature with slight lift
                list.add(GameCoin(id++, x, y - 45f))
                
                // Add consecutive trail of coins in some places
                if (x.toInt() % 70 == 0) {
                    list.add(GameCoin(id++, x + 20f, getTrackHeight(trackId, x + 20f) - 50f))
                    list.add(GameCoin(id++, x + 40f, getTrackHeight(trackId, x + 40f) - 55f))
                }
            }
            x += 80f // space spacing
        }
        return list
    }

    private fun getGapBounds(trackId: String, x: Float): Pair<Float, Float> {
        return when (trackId) {
            "canyon" -> {
                if (x in 520f..650f) 550f to 620f
                else if (x in 1370f..1510f) 1400f to 1480f
                else 0f to 0f
            }
            "tokyo" -> {
                if (x in 770f..910f) 800f to 880f
                else if (x in 1570f..1690f) 1600f to 1660f
                else 0f to 0f
            }
            "arctic" -> {
                if (x in 670f..820f) 700f to 790f
                else 0f to 0f
            }
            "volcano" -> {
                if (x in 420f..560f) 450f to 530f
                else if (x in 1120f..1270f) 1150f to 1240f
                else if (x in 1820f..1980f) 1850f to 1950f
                else 0f to 0f
            }
            else -> 0f to 0f
        }
    }

    /**
     * Generates custom visual decos (cactus for canyon, electric post for Tokyo, etc.)
     */
    fun generateDecosForTrack(trackId: String, length: Float): List<EnvironmentalDeco> {
        val list = mutableListOf<EnvironmentalDeco>()
        val rand = java.util.Random(42) // Seeded for consistency
        var x = 80f
        val type = when (trackId) {
            "canyon" -> "cactus"
            "tokyo" -> "neon_tower"
            "arctic" -> "pine_tree"
            "volcano" -> "lava_geyser"
            else -> "cactus"
        }
        while (x < length - 150f) {
            val y = getTrackHeight(trackId, x)
            if (y < 800f) { // do not place decorations in gaps
                val scale = 0.7f + rand.nextFloat() * 0.6f
                list.add(EnvironmentalDeco(x, type, scale))
            }
            x += 160f + rand.nextFloat() * 200f
        }
        return list
    }

    /**
     * Generates booster tiles
     */
    fun generateBoostPadsForTrack(trackId: String, length: Float): List<BoostPad> {
        val list = mutableListOf<BoostPad>()
        when (trackId) {
            "canyon" -> {
                list.add(BoostPad(300f))
                list.add(BoostPad(1200f))
            }
            "tokyo" -> {
                list.add(BoostPad(250f))
                list.add(BoostPad(650f))
                list.add(BoostPad(1100f))
                list.add(BoostPad(1500f))
                list.add(BoostPad(2100f))
            }
            "arctic" -> {
                list.add(BoostPad(450f))
                list.add(BoostPad(1050f))
                list.add(BoostPad(1650f))
            }
            "volcano" -> {
                list.add(BoostPad(350f))
                list.add(BoostPad(800f))
                list.add(BoostPad(1400f))
                list.add(BoostPad(1750f))
            }
        }
        return list
    }
}
