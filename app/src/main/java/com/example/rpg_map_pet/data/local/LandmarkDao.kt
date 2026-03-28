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

    /**
     * Загрузка меток в прямоугольной области (bounding box).
     * Используется для загрузки меток в видимой области карты.
     */
    @Query("""
        SELECT * FROM landmarks 
        WHERE latitude >= :minLatitude 
          AND latitude <= :maxLatitude 
          AND longitude >= :minLongitude 
          AND longitude <= :maxLongitude
    """)
    fun getLandmarksInBoundingBox(
        minLatitude: Double,
        maxLatitude: Double,
        minLongitude: Double,
        maxLongitude: Double
    ): Flow<List<LandmarkEntity>>

    /**
     * Загрузка меток в радиусе от точки.
     * Использует упрощённую формулу (без Haversine) для производительности.
     */
    @Query("""
        SELECT * FROM landmarks 
        WHERE (
            6371000 * acos(
                cos(radians(:latitude)) * cos(radians(latitude)) *
                cos(radians(longitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(latitude))
            )
        ) <= :radiusMeters
    """)
    fun getLandmarksInRadius(
        latitude: Double,
        longitude: Double,
        radiusMeters: Float
    ): Flow<List<LandmarkEntity>>

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
