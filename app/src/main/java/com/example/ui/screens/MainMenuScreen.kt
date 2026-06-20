package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GameViewModel

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfileState.collectAsState()
    val scrollState = rememberScrollState()
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Glowing background accents
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehindBackgroundGrid(colors.outline.copy(alpha = 0.2f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Title Block
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🏍️",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "APEX RIDER",
                    color = colors.onBackground,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 2.sp
                )
            }
            Text(
                text = "VELOCITY PRO SIMULATOR",
                color = colors.primary, // #D0BCFF ACCENT
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Dynamic Hero Graphics Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, colors.outline, RoundedCornerShape(24.dp))
                    .shadow(8.dp, RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_game_cover),
                    contentDescription = "Apex Rider Cover art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dark bottom overlay gradient matching background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, colors.background.copy(alpha = 0.85f))
                            )
                        )
                )
                // Active Bike Tag
                Text(
                    text = "READY TO LAUNCH",
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(colors.primary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Wallet/Coin Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface) // #2B2930 CARD BACKGROUND
                    .border(1.dp, colors.outline, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💳 ",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "YOUR BALANCE",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${profile?.coins ?: 0}",
                        color = colors.primary, // Match Accent color
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🪙",
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Menu List
            MenuActionCard(
                title = "LAUNCH COMPETE",
                subtitle = "Take customizable bikes onto multiple terrains",
                icon = Icons.Default.PlayArrow,
                accentColor = colors.primary, // #D0BCFF ACCENT
                tagId = "launch_compete_btn",
                onClick = { viewModel.navigateTo("TrackSelect") }
            )

            Spacer(modifier = Modifier.height(14.dp))

            MenuActionCard(
                title = "PADDOCK GARAGE",
                subtitle = "Customize colors and upgrade motor specs",
                icon = Icons.Default.Settings,
                accentColor = colors.primary,
                tagId = "paddock_garage_btn",
                onClick = { viewModel.navigateTo("Garage") }
            )

            Spacer(modifier = Modifier.height(14.dp))

            MenuActionCard(
                title = "SIMULATOR SETTINGS",
                subtitle = "Manage options & view stunt mechanics",
                icon = Icons.Default.Info,
                accentColor = colors.primary,
                tagId = "simulator_settings_btn",
                onClick = { viewModel.navigateTo("Settings") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Proactive Developer Cheat Button for easy testing and review!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.background)
                    .border(1.dp, colors.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { viewModel.addCheatCoins() }
                    .padding(14.dp)
                    .testTag("cheat_coin_button"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BOOST COINS CHEAT (+500🪙)",
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MenuActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    tagId: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tagId)
            .clickable(onClick = onClick)
            .border(1.dp, colors.outline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface), // #2B2930 CARD BACKGROUND
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon frame
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = colors.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = subtitle,
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Draws a beautiful diagonal tech grid behind the menus.
 */
fun Modifier.drawBehindBackgroundGrid(gridColor: Color): Modifier = drawBehind {
    val step = 40f
    val strokeWidth = 1f

    // Vertical / Horizontal lines
    for (x in 0..size.width.toInt() step step.toInt()) {
        drawLine(
            color = gridColor,
            start = androidx.compose.ui.geometry.Offset(x.toFloat(), 0f),
            end = androidx.compose.ui.geometry.Offset(x.toFloat(), size.height),
            strokeWidth = strokeWidth
        )
    }
    for (y in 0..size.height.toInt() step step.toInt()) {
        drawLine(
            color = gridColor,
            start = androidx.compose.ui.geometry.Offset(0f, y.toFloat()),
            end = androidx.compose.ui.geometry.Offset(size.width, y.toFloat()),
            strokeWidth = strokeWidth
        )
    }
}

