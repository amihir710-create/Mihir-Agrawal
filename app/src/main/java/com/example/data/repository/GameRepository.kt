package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val gameDao: GameDao) {

    val userProfileFlow: Flow<UserProfile?> = gameDao.getUserProfileFlow()
    val allBikesFlow: Flow<List<BikeEntity>> = gameDao.getAllBikesFlow()
    val allTracksFlow: Flow<List<TrackEntity>> = gameDao.getAllTracksFlow()

    suspend fun ensureInitialized() {
        val profile = gameDao.getUserProfile()
        if (profile == null) {
            // Seed default User Profile
            gameDao.insertUserProfile(UserProfile())
            
            // Seed initial Bikes
            val initialBikes = listOf(
                BikeEntity(
                    bikeId = "scout",
                    isUnlocked = true,
                    engineLevel = 1,
                    suspensionLevel = 1,
                    tiresLevel = 1,
                    weightLevel = 1,
                    primaryColorHex = "#E11D48", // Rose/Crimson
                    secondaryColorHex = "#1E293B" // Slate 800
                ),
                BikeEntity(
                    bikeId = "ninja",
                    isUnlocked = false,
                    engineLevel = 1,
                    suspensionLevel = 1,
                    tiresLevel = 1,
                    weightLevel = 1,
                    primaryColorHex = "#10B981", // Emerald Green
                    secondaryColorHex = "#111827" // Grey 900
                ),
                BikeEntity(
                    bikeId = "titan",
                    isUnlocked = false,
                    engineLevel = 1,
                    suspensionLevel = 1,
                    tiresLevel = 1,
                    weightLevel = 1,
                    primaryColorHex = "#F59E0B", // Amber Orange
                    secondaryColorHex = "#374151" // Grey 700
                ),
                BikeEntity(
                    bikeId = "photon",
                    isUnlocked = false,
                    engineLevel = 1,
                    suspensionLevel = 1,
                    tiresLevel = 1,
                    weightLevel = 1,
                    primaryColorHex = "#06B6D4", // Cyan
                    secondaryColorHex = "#581C87" // Purple 900
                )
            )
            gameDao.insertBikes(initialBikes)

            // Seed initial Tracks
            val initialTracks = listOf(
                TrackEntity(trackId = "canyon", isUnlocked = true, bestTimeMillis = -1L),
                TrackEntity(trackId = "tokyo", isUnlocked = false, bestTimeMillis = -1L),
                TrackEntity(trackId = "arctic", isUnlocked = false, bestTimeMillis = -1L),
                TrackEntity(trackId = "volcano", isUnlocked = false, bestTimeMillis = -1L)
            )
            gameDao.insertTracks(initialTracks)
        }
    }

    suspend fun addCoins(amount: Int) {
        val current = gameDao.getUserProfile() ?: UserProfile()
        val updated = current.copy(coins = current.coins + amount)
        gameDao.insertUserProfile(updated)
    }

    suspend fun selectBike(bikeId: String) {
        val current = gameDao.getUserProfile() ?: UserProfile()
        val updated = current.copy(selectedBikeId = bikeId)
        gameDao.insertUserProfile(updated)
    }

    suspend fun selectTrack(trackId: String) {
        val current = gameDao.getUserProfile() ?: UserProfile()
        val updated = current.copy(selectedTrackId = trackId)
        gameDao.insertUserProfile(updated)
    }

    suspend fun unlockBike(bikeId: String, cost: Int): Boolean {
        val profile = gameDao.getUserProfile() ?: return false
        if (profile.coins >= cost) {
            val bike = gameDao.getBikeById(bikeId) ?: return false
            if (!bike.isUnlocked) {
                // Deduct coins & unlock bike
                val updatedProfile = profile.copy(coins = profile.coins - cost)
                gameDao.insertUserProfile(updatedProfile)
                
                val updatedBike = bike.copy(isUnlocked = true)
                gameDao.insertBike(updatedBike)
                return true
            }
        }
        return false
    }

    suspend fun unlockTrack(trackId: String, cost: Int): Boolean {
        val profile = gameDao.getUserProfile() ?: return false
        if (profile.coins >= cost) {
            // Find track progress
            val allTracks = gameDao.getAllTracksFlow().firstOrNull() ?: emptyList()
            val track = allTracks.find { it.trackId == trackId } ?: return false
            if (!track.isUnlocked) {
                // Deduct coins & unlock track
                val updatedProfile = profile.copy(coins = profile.coins - cost)
                gameDao.insertUserProfile(updatedProfile)
                
                val updatedTrack = track.copy(isUnlocked = true)
                gameDao.insertTrack(updatedTrack)
                return true
            }
        }
        return false
    }

    suspend fun upgradeBikeAttribute(bikeId: String, attribute: String, cost: Int): Boolean {
        val profile = gameDao.getUserProfile() ?: return false
        if (profile.coins >= cost) {
            val bike = gameDao.getBikeById(bikeId) ?: return false
            if (bike.isUnlocked) {
                val updatedBike = when (attribute.lowercase()) {
                    "engine" -> if (bike.engineLevel < 5) bike.copy(engineLevel = bike.engineLevel + 1) else null
                    "suspension" -> if (bike.suspensionLevel < 5) bike.copy(suspensionLevel = bike.suspensionLevel + 1) else null
                    "tires" -> if (bike.tiresLevel < 5) bike.copy(tiresLevel = bike.tiresLevel + 1) else null
                    "weight" -> if (bike.weightLevel < 5) bike.copy(weightLevel = bike.weightLevel + 1) else null
                    else -> null
                } ?: return false

                val updatedProfile = profile.copy(coins = profile.coins - cost)
                gameDao.insertUserProfile(updatedProfile)
                gameDao.insertBike(updatedBike)
                return true
            }
        }
        return false
    }

    suspend fun updateBikeColors(bikeId: String, primaryHex: String, secondaryHex: String) {
        val bike = gameDao.getBikeById(bikeId) ?: return
        val updated = bike.copy(primaryColorHex = primaryHex, secondaryColorHex = secondaryHex)
        gameDao.insertBike(updated)
    }

    suspend fun saveBestTime(trackId: String, timeMillis: Long): Boolean {
        val allTracks = gameDao.getAllTracksFlow().firstOrNull() ?: emptyList()
        val track = allTracks.find { it.trackId == trackId } ?: return false
        val isNewRecord = track.bestTimeMillis == -1L || timeMillis < track.bestTimeMillis
        if (isNewRecord) {
            val updated = track.copy(bestTimeMillis = timeMillis)
            gameDao.insertTrack(updated)
            return true
        }
        return false
    }
}
