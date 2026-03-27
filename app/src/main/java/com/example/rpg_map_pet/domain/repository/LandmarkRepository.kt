package com.example.rpg_map_pet.domain.repository

import com.example.rpg_map_pet.data.local.GeoJsonImporter
import com.example.rpg_map_pet.domain.model.Landmark
import kotlinx.coroutines.flow.Flow

interface LandmarkRepository {
    fun getLandmarks(): Flow<List<Landmark>>
    fun getActiveLandmarks(): Flow<List<Landmark>>
    fun getCompletedLandmarks(): Flow<List<Landmark>>
    suspend fun getLandmarkById(landmarkId: String): Landmark?
    suspend fun markAsVisited(landmarkId: String)
    suspend fun markAsNotVisited(landmarkId: String)
    fun isUserNearLandmark(userLat: Double, userLng: Double, landmark: Landmark): Boolean
    suspend fun importFromGeoJson(): GeoJsonImporter.ImportResult
    suspend fun getLandmarksCount(): Int
}
