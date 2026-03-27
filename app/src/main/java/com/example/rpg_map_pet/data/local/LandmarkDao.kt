package com.example.rpg_map_pet.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LandmarkDao {

    @Query("SELECT * FROM landmarks")
    fun getAllLandmarks(): Flow<List<LandmarkEntity>>

    @Query("SELECT * FROM landmarks WHERE id = :landmarkId")
    suspend fun getLandmarkById(landmarkId: String): LandmarkEntity?

    @Query("SELECT * FROM landmarks WHERE isCompleted = 0")
    fun getActiveLandmarks(): Flow<List<LandmarkEntity>>

    @Query("SELECT * FROM landmarks WHERE isCompleted = 1")
    fun getCompletedLandmarks(): Flow<List<LandmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandmark(landmark: LandmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandmarks(landmarks: List<LandmarkEntity>)

    @Update
    suspend fun updateLandmark(landmark: LandmarkEntity)

    @Query("UPDATE landmarks SET isCompleted = 1 WHERE id = :landmarkId")
    suspend fun markAsCompleted(landmarkId: String)

    @Query("DELETE FROM landmarks WHERE id = :landmarkId")
    suspend fun deleteLandmark(landmarkId: String)

    @Query("DELETE FROM landmarks")
    suspend fun deleteAllLandmarks()

    @Query("SELECT COUNT(*) FROM landmarks")
    suspend fun getLandmarksCount(): Int
}
