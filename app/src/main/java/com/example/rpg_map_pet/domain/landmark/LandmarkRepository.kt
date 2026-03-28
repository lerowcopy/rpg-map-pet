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
}

sealed class ImportResult {
    data class Success(val count: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
    object AlreadyImported : ImportResult()
}
