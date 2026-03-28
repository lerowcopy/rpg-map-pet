package com.example.rpg_map_pet.domain.location

import com.example.rpg_map_pet.core.base.FlowUseCase
import com.example.rpg_map_pet.core.base.ResultUseCaseNoParams
import com.example.rpg_map_pet.core.result.Result
import com.example.rpg_map_pet.domain.model.UserLocation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting continuous location updates.
 */
class GetLocationUpdates @Inject constructor(
    private val repository: LocationRepository
) : FlowUseCase<UserLocation> {
    override operator fun invoke(): Flow<UserLocation> = repository.getLocationUpdates()
}

/**
 * Use case for getting the current location once.
 */
class GetCurrentLocation @Inject constructor(
    private val repository: LocationRepository
) : ResultUseCaseNoParams<UserLocation> {
    override suspend operator fun invoke(): Result<UserLocation> = repository.getCurrentLocation()
}
