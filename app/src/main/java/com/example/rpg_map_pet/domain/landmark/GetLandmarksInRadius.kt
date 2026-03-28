package com.example.rpg_map_pet.domain.landmark

import com.example.rpg_map_pet.domain.model.Landmark
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case для загрузки меток в радиусе от заданной точки.
 * @param radiusMeters Радиус в метрах (рекомендуется 5000-10000)
 */
class GetLandmarksInRadius @Inject constructor(
    private val repository: LandmarkRepository
) {
    data class Params(
        val latitude: Double,
        val longitude: Double,
        val radiusMeters: Float = 5000f
    )

    operator fun invoke(params: Params): Flow<List<Landmark>> {
        return repository.getLandmarksInRadius(
            params.latitude,
            params.longitude,
            params.radiusMeters
        )
    }
}
