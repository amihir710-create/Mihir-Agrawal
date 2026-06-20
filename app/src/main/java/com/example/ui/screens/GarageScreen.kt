package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.BikeEntity
import com.example.ui.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfileState.collectAsState()
    val bikes by viewModel.bikesState.collectAsState()
    val colors = MaterialTheme.colorScheme

    // Manage locally selected tab/bike for viewing
    var localSelectedBikeId by remember { mutableStateOf("scout") }

    val activeViewBike = bikes.find { it.bikeId == localSelectedBikeId } ?: BikeEntity(
        bikeId = "scout", isUnlocked = true, primaryColorHex = "#E11D48", secondaryColorHex = "#1E293B"
    )

    val scrollState = rememberScrollState()

    // Definitions of static assets / labels per bike ID
    val bikeMetadata = mapOf(
        "scout" to BikeMeta("Scout Cruiser", "Stable chopper with balanced speed", 0),
        "ninja" to BikeMeta("Ninja X-12", "Fierce torque, rapid acceleration", 250),
        "titan" to BikeMeta("Titan Enduro", "Heavy chassis, supreme air shocks", 600),
        "photon" to BikeMeta("Electro-Photon", "Sci-fi hover-tier magnetic frame", 1200)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "BIKE PADDOCK GARAGE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 1.sp,
                        color = colors.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo("MainMenu") },
                        modifier = Modifier.testTag("garage_back_btn")
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
                            color = colors.primary, // #D0BCFF Accent
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
            // Horizontal Selection of Bike Cards
            Text(
                text = "CHOOSE MODEL",
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(bikes) { bike ->
                    val meta = bikeMetadata[bike.bikeId] ?: BikeMeta("Unknown", "Specs", 0)
                    val isCurrentEquipped = profile?.selectedBikeId == bike.bikeId
                    val isViewed = localSelectedBikeId == bike.bikeId

                    Card(
                        modifier = Modifier
                            .width(180.dp)
                            .testTag("bike_card_${bike.bikeId}")
                            .clickable { localSelectedBikeId = bike.bikeId },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isViewed) colors.surface else colors.surface.copy(alpha = 0.5f)
                        ),
                        border = borderStroke(isViewed, colors.primary, colors.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Bike schematic representation icon
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(bike.primaryColorHex)).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🏍️",
                                    fontSize = 28.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = meta.name,
                                color = colors.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            if (isCurrentEquipped) {
                                BadgeText("EQUIPPED", Color(0xFF81C784))
                            } else if (bike.isUnlocked) {
                                BadgeText("OWNED", colors.onSurfaceVariant.copy(alpha = 0.6f))
                            } else {
                                BadgeText("${meta.cost} 🪙", colors.primary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Details Panel of Selected Bike Model
            val meta = bikeMetadata[activeViewBike.bikeId] ?: BikeMeta("Unknown", "Specs", 0)
            
            // Bike Model Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outline, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface), // #2B2930 CARD BACKGROUND
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = meta.name.uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = meta.subtitle,
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    HorizontalDivider(color = colors.outline, modifier = Modifier.padding(bottom = 16.dp))

                    // Engine Spec Range
                    AttributeBar(
                        label = "ENGINE (TOP SPEED)",
                        level = activeViewBike.engineLevel,
                        description = "${14f + (activeViewBike.engineLevel - 1) * 2.2f} m/s",
                        attributeName = "engine",
                        bike = activeViewBike,
                        viewModel = viewModel,
                        coins = profile?.coins ?: 0
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Suspension Spec Range
                    AttributeBar(
                        label = "SUSPENSION (STABILITY)",
                        level = activeViewBike.suspensionLevel,
                        description = "Cushion lvl ${activeViewBike.suspensionLevel}",
                        attributeName = "suspension",
                        bike = activeViewBike,
                        viewModel = viewModel,
                        coins = profile?.coins ?: 0
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tires Spec Range
                    AttributeBar(
                        label = "TIRES (GRIP)",
                        level = activeViewBike.tiresLevel,
                        description = "Friction x${1.0f + (activeViewBike.tiresLevel - 1) * 0.18f}",
                        attributeName = "tires",
                        bike = activeViewBike,
                        viewModel = viewModel,
                        coins = profile?.coins ?: 0
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Weight Spec Range
                    AttributeBar(
                        label = "CHASSIS (WEIGHT COMPACT)",
                        level = activeViewBike.weightLevel,
                        description = "Weight density ${activeViewBike.weightLevel}",
                        attributeName = "weight",
                        bike = activeViewBike,
                        viewModel = viewModel,
                        coins = profile?.coins ?: 0
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Color Picker System for unlocked bikes!
                    if (activeViewBike.isUnlocked) {
                        Text(
                            text = "CUSTOM PAINT WORK",
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Primary Colors
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Primary:", color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.width(64.dp))
                            val primaryColors = listOf("#E11D48", "#2563EB", "#16A34A", "#CA8A04", "#7C3AED", "#06B6D4")
                            primaryColors.forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(3.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(
                                            width = if (activeViewBike.primaryColorHex == hex) 2.dp else 0.dp,
                                            color = colors.primary,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            viewModel.customizeColors(
                                                activeViewBike.bikeId,
                                                hex,
                                                activeViewBike.secondaryColorHex
                                            )
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Secondary Colors
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Trim:", color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.width(64.dp))
                            val secondaryColors = listOf("#0F172A", "#FFFFFF", "#334155", "#475569", "#701A75", "#F43F5E")
                            secondaryColors.forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(3.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(
                                            width = if (activeViewBike.secondaryColorHex == hex) 2.dp else 0.dp,
                                            color = colors.primary,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            viewModel.customizeColors(
                                                activeViewBike.bikeId,
                                                activeViewBike.primaryColorHex,
                                                hex
                                            )
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Purchase or Equip Button
                    if (!activeViewBike.isUnlocked) {
                        Button(
                            onClick = { viewModel.purchaseBike(activeViewBike.bikeId, meta.cost) },
                            enabled = (profile?.coins ?: 0) >= meta.cost,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("purchase_bike_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary,
                                disabledContainerColor = colors.secondary.copy(alpha = 0.5f),
                                disabledContentColor = colors.onSecondary.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "UNLOCK MOTORCYCLE - ${meta.cost} 🪙",
                                color = colors.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        val isEquipped = profile?.selectedBikeId == activeViewBike.bikeId
                        Button(
                            onClick = {
                                viewModel.selectBike(activeViewBike.bikeId)
                            },
                            enabled = !isEquipped,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("equip_bike_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary,
                                disabledContainerColor = colors.secondary,
                                disabledContentColor = colors.onSurfaceVariant.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                if (isEquipped) "CURRENTLY MOUNTED" else "EQUIP BIKE MODEL",
                                color = if (isEquipped) colors.onSurfaceVariant.copy(alpha = 0.7f) else colors.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttributeBar(
    label: String,
    level: Int,
    description: String,
    attributeName: String,
    bike: BikeEntity,
    viewModel: GameViewModel,
    coins: Int
) {
    val upgradeCost = level * 75 // upgrades cost scaling: 75, 150, 225, 300
    val isMaxed = level >= 5
    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "$label ",
                color = colors.onSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = description,
                color = colors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bars representing levels
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 1..5) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (i <= level) colors.primary else colors.secondary
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Upgrade action button
            if (bike.isUnlocked) {
                Button(
                    onClick = { viewModel.upgradeAttribute(bike.bikeId, attributeName, upgradeCost) },
                    enabled = !isMaxed && coins >= upgradeCost,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("upgrade_${attributeName}_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary,
                        disabledContainerColor = colors.secondary.copy(alpha = 0.5f),
                        disabledContentColor = colors.onSurfaceVariant.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isMaxed) "MAX" else "+1 Lvl [ $upgradeCost🪙 ]",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMaxed) colors.onSurfaceVariant.copy(alpha = 0.4f) else colors.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeText(text: String, backgroundColor: Color) {
    Text(
        text = text,
        color = Color.Black,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

data class BikeMeta(val name: String, val subtitle: String, val cost: Int)

fun borderStroke(is_viewed: Boolean, viewed_color: Color, unviewed_color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(
        width = if (is_viewed) 2.dp else 1.dp,
        color = if (is_viewed) viewed_color else unviewed_color
    )
}
