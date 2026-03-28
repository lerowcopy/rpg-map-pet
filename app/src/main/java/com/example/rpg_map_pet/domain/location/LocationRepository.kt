package com.example.rpg_map_pet.domain.location

import com.example.rpg_map_pet.core.result.Result
import com.example.rpg_map_pet.domain.model.UserLocation
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for location operations.
 * Part of the domain layer - defines the contract for location data.
 */
interface LocationRepository {
    fun getLocationUpdates(): Flow<UserLocation>
    suspend fun getCurrentLocation(): Result<UserLocation>
}
