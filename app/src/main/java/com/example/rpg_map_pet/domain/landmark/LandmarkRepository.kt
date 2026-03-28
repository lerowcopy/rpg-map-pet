package com.example.rpg_map_pet.domain.landmark

import com.example.rpg_map_pet.domain.model.Landmark
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for landmark operations.
 * Part of the domain layer - defines the contract for landmark data.
 */
interface LandmarkRepository {
    fun getLandmarks(): Flow<List<Landmark>>
    fun getActiveLandmarks(): Flow<List<Landmark>>
    fun getCompletedLandmarks(): Flow<List<Landmark>>
    suspend fun getLandmarkById(landmarkId: String): Landmark?
    suspend fun markAsVisited(landmarkId: String)
    suspend fun markAsNotVisited(landmarkId: String)
    fun isUserNearLandmark(userLat: Double, userLng: Double, landmark: Landmark): Boolean
    suspend fun importFromGeoJson(): ImportResult
    suspend fun getLandmarksCount(): Int

    /**
     * Загрузка меток в прямоугольной области (bounding box).
     */
    fun getLandmarksInBoundingBox(
        minLatitude: Double,
        maxLatitude: Double,
        minLongitude: Double,
        maxLongitude: Double
    ): Flow<List<Landmark>>

    /**
     * Загрузка меток в радиусе от точки.
     */
    fun getLandmarksInRadius(
        latitude: Double,
        longitude: Double,
        radiusMeters: Float
    ): Flow<List<Landmark>>

    /**
     * Получить фотографии метки.
     */
    fun getLandmarkPhotos(landmarkId: String): Flow<List<String>>

    /**
     * Получить фотографии метки с ID (для UI).
     */
    fun getLandmarkPhotosWithIds(landmarkId: String): Flow<List<com.example.rpg_map_pet.data.local.LandmarkPhotoEntity>>

    /**
     * Добавить фотографию к метке.
     */
    suspend fun addLandmarkPhoto(landmarkId: String, photoPath: String)

    /**
     * Удалить фотографию метки.
     */
    suspend fun deleteLandmarkPhoto(photoId: Long)
}

sealed class ImportResult {
    data class Success(val count: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
    object AlreadyImported : ImportResult()
}
