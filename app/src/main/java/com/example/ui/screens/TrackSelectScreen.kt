package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.TrackData
import com.example.game.TrackInfo
import com.example.ui.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackSelectScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfileState.collectAsState()
    val dbTracks by viewModel.tracksState.collectAsState()
    val scrollState = rememberScrollState()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WORLD COMPETITIONS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 1.sp,
                        color = colors.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo("MainMenu") },
                        modifier = Modifier.testTag("track_select_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onBackground
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${profile?.coins ?: 0}",
                            color = colors.primary, // #D0BCFF ACCENT
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "🪙", fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground
                )
            )
        },
        containerColor = colors.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Text(
                text = "SELECT RACING DEPOT",
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Loop 4 Tracks
            TrackData.TRACKS.forEach { trackInfo ->
                val dbTrack = dbTracks.find { it.trackId == trackInfo.id }
                val isUnlocked = dbTrack?.isUnlocked ?: (trackInfo.unlockCost == 0)
                val bestTime = dbTrack?.bestTimeMillis ?: -1L

                TrackRowCard(
                    track = trackInfo,
                    isUnlocked = isUnlocked,
                    bestTime = bestTime,
                    profileCoins = profile?.coins ?: 0,
                    selectedBikeId = profile?.selectedBikeId ?: "scout",
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun TrackRowCard(
    track: TrackInfo,
    isUnlocked: Boolean,
    bestTime: Long,
    profileCoins: Int,
    selectedBikeId: String,
    viewModel: GameViewModel
) {
    val colors = MaterialTheme.colorScheme
    val difficultyColor = when (track.difficulty.lowercase()) {
        "easy" -> Color(0xFF81C784)
        "medium" -> Color(0xFFFFB74D)
        "hard" -> Color(0xFFE57373)
        "insane" -> colors.primary
        else -> colors.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("track_row_${track.id}")
            .border(1.dp, colors.outline, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface), // #2B2930 CARD BACKGROUND
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Image Box with custom gradients
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.linearGradient(
                            colors = track.skyColors
                        )
                    )
                    .padding(16.dp)
            ) {
                // Vector decorative lines
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val curveColor = track.pathStrokeColor.copy(alpha = 0.5f)
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(0f, this@drawBehind.size.height * 0.75f)
                                cubicTo(
                                    this@drawBehind.size.width * 0.25f, this@drawBehind.size.height * 0.25f,
                                    this@drawBehind.size.width * 0.5f, this@drawBehind.size.height * 0.95f,
                                    this@drawBehind.size.width * 0.75f, this@drawBehind.size.height * 0.45f
                                )
                                lineTo(this@drawBehind.size.width, this@drawBehind.size.height * 0.65f)
                            }
                            this@drawBehind.drawPath(
                                path = path,
                                color = curveColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 10f,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            )
                        }
                )

                // Difficulty Overlay
                Text(
                    text = track.difficulty.uppercase(),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(difficultyColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )

                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Text(
                        text = track.name.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${(track.length / 1000f)} KM COOP CIRCUIT",
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // Description and Info Body
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = track.description,
                    color = colors.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Gravity Info
                    Column {
                        Text(text = "GRAVITY", color = colors.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${track.baseGravity}G", color = colors.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }

                    // Slide Multiplier / Slippery Index
                    Column {
                        Text(text = "TRACTION", color = colors.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        val slideStateLabel = when {
                            track.slideMultiplier > 1.4f -> "Slippery"
                            track.slideMultiplier < 0.9f -> "High Traction"
                            else -> "Standard"
                        }
                        Text(text = slideStateLabel, color = colors.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }

                    // Player High Score Best Time
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "BEST RECORD", color = colors.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        val recordText = if (bestTime == -1L) {
                            "Unchallenged"
                        } else {
                            formatTime(bestTime)
                        }
                        Text(text = recordText, color = colors.primary, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Control / Unlock Buttons
                if (isUnlocked) {
                    Button(
                        onClick = { viewModel.startGame(track.id, selectedBikeId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("race_track_btn_${track.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary, // Elegant Dark Accent Button Bg (#D0BCFF)
                            contentColor = colors.onPrimary // Elegant Dark Accent Button Text (#381E72)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = colors.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ENTER COMPETITION RACE 🏁",
                            color = colors.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.purchaseTrack(track.id, track.unlockCost) },
                        enabled = profileCoins >= track.unlockCost,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("unlock_track_btn_${track.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.secondary, // Matte Card Button background
                            contentColor = colors.onSecondary,
                            disabledContainerColor = colors.secondary.copy(alpha = 0.4f),
                            disabledContentColor = colors.onSecondary.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outline)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (profileCoins >= track.unlockCost) colors.onSecondary else colors.onSecondary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "UNLOCK DEPOT MAP🔒 - ${track.unlockCost} 🪙",
                            color = if (profileCoins >= track.unlockCost) colors.onSecondary else colors.onSecondary.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Returns formatted stopwatch string: mm:ss.SS
 */
fun formatTime(timeMillis: Long): String {
    val mins = timeMillis / 60000
    val secs = (timeMillis % 60000) / 1000
    val mills = (timeMillis % 1000) / 10
    return String.format("%02d:%02d.%02d", mins, secs, mills)
}
