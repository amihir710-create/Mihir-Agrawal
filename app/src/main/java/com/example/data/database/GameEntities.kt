package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 100, // Starts with 100 coins
    val selectedBikeId: String = "scout",
    val selectedTrackId: String = "canyon",
    val isAudioEnabled: Boolean = true
)

@Entity(tableName = "bike_customization")
data class BikeEntity(
    @PrimaryKey val bikeId: String,
    val isUnlocked: Boolean,
    val engineLevel: Int = 1,
    val suspensionLevel: Int = 1,
    val tiresLevel: Int = 1,
    val weightLevel: Int = 1,
    val primaryColorHex: String,
    val secondaryColorHex: String
)

@Entity(tableName = "track_progress")
data class TrackEntity(
    @PrimaryKey val trackId: String,
    val isUnlocked: Boolean,
    val bestTimeMillis: Long = -1L // -1 means no record yet
)
