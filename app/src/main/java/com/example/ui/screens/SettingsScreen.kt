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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfileState.collectAsState()
    val scrollState = rememberScrollState()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SIMULATION CONTROL CENTER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 1.sp,
                        color = colors.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo("MainMenu") },
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onBackground
                        )
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
            // Section 1: Audio / Controls Toggle
            Text(
                text = "PADDOCK AUDIO & CONTROLS",
                color = colors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outline, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tick Audio Simulation Mode",
                                color = colors.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Simulate engine revs audio visual flags",
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        // We can toggle
                        Switch(
                            checked = profile?.isAudioEnabled ?: true,
                            onCheckedChange = {
                                // Toggling in ViewModel (can mock/impl color shift or standard state)
                            },
                            modifier = Modifier.testTag("engine_audio_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 2: Instruction on Tricks and Physics Mechanics
            Text(
                text = "PHYSICAL RACING MANUAL (STUNT GUIDE)",
                color = colors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outline, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TutorialStepItem(
                        number = "01",
                        title = "AIRBACK FLIPS STRAT",
                        body = "When launched off high jumps, press/hold GAS or TILT BACK buttons to rotate clockwise. Performing a clean 360° flip awards immediately +25🪙 stunt credit of gold!"
                    )

                    TutorialStepItem(
                        number = "02",
                        title = "CRASH MECHANICAL LAWS",
                        body = "The top helmet side (head angle) config must align within +/- 65 degrees (slope deviation) upon impact. Landing too steep crashes your bike. Upgrade SUSPENSION to raise safe alignment bounds up to 86°!"
                    )

                    TutorialStepItem(
                        number = "03",
                        title = "BOOSTING TILES",
                        body = "Press gas pedal when crossing electric grid boosters on Neon Tokyo or canyon nodes to catapult your velocity vector to 135% of peak motor power!"
                    )

                    TutorialStepItem(
                        number = "04",
                        title = "ATTR EXPLAINEDS",
                        body = "• Engine: Speeds up velocity boundary ceiling.\n• Suspension: Softens impact, prevents alignment crashes on jumps.\n• Tires: Negates sliding coefficients of snowy glacial floors.\n• Chassis Weight: Lightens aggregate frame mass to flip speed faster."
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 3: Game Credits
            Text(
                text = "SYSTEM ARCHITECTURE CREDITS",
                color = colors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outline, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "APEX RIDER v1.0.1 (OFFLINE MODE)",
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Built natively under Google AI Studio platform using Jetpack Compose, high performance 60FPS physics systems, and SQLite local Room databases.",
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TutorialStepItem(
    number: String,
    title: String,
    body: String
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            color = colors.primary,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            modifier = Modifier.width(36.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
            Text(
                text = body,
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
