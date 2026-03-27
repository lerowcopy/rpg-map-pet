package com.example.rpg_map_pet.domain.repository

import com.example.rpg_map_pet.domain.model.Landmark
import kotlinx.coroutines.flow.Flow

interface LandmarkRepository {
    fun getLandmarks(): Flow<List<Landmark>>
    suspend fun markAsVisited(landmarkId: String)
    fun isUserNearLandmark(userLat: Double, userLng: Double, landmark: Landmark): Boolean
}
