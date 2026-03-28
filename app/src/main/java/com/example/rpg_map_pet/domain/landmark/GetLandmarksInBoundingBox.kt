package com.example.rpg_map_pet.domain.landmark

import com.example.rpg_map_pet.domain.model.Landmark
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case для загрузки меток в прямоугольной области (bounding box).
 * Используется для загрузки меток в видимой области карты.
 */
class GetLandmarksInBoundingBox @Inject constructor(
    private val repository: LandmarkRepository
) {
    data class Params(
        val minLatitude: Double,
        val maxLatitude: Double,
        val minLongitude: Double,
        val maxLongitude: Double
    )

    operator fun invoke(params: Params): Flow<List<Landmark>> {
        return repository.getLandmarksInBoundingBox(
            params.minLatitude,
            params.maxLatitude,
            params.minLongitude,
            params.maxLongitude
        )
    }
}
