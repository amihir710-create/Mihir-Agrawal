package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    @Query("SELECT * FROM bike_customization")
    fun getAllBikesFlow(): Flow<List<BikeEntity>>

    @Query("SELECT * FROM bike_customization WHERE bikeId = :bikeId")
    suspend fun getBikeById(bikeId: String): BikeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBike(bike: BikeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBikes(bikes: List<BikeEntity>)

    @Query("SELECT * FROM track_progress")
    fun getAllTracksFlow(): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)
}
