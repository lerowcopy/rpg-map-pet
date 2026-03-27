package com.example.rpg_map_pet.domain.repository

import com.example.rpg_map_pet.domain.model.UserLocation
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLocationUpdates(): Flow<UserLocation>
    suspend fun getCurrentLocation(): Result<UserLocation>
}
