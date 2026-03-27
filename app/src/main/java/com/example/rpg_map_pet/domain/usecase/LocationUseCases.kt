package com.example.rpg_map_pet.domain.usecase

import com.example.rpg_map_pet.domain.model.UserLocation
import com.example.rpg_map_pet.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class GetLocationUpdates(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): Flow<UserLocation> = locationRepository.getLocationUpdates()
}

class GetCurrentLocation(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): Result<UserLocation> = locationRepository.getCurrentLocation()
}
