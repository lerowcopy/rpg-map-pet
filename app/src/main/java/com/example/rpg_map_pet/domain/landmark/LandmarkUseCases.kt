package com.example.rpg_map_pet.domain.landmark

import com.example.rpg_map_pet.core.base.FlowUseCase
import com.example.rpg_map_pet.core.base.ResultUseCase
import com.example.rpg_map_pet.core.base.ResultUseCaseNoParams
import com.example.rpg_map_pet.core.result.Result
import com.example.rpg_map_pet.domain.model.Landmark
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all landmarks.
 */
class GetLandmarks @Inject constructor(
    private val repository: LandmarkRepository
) : FlowUseCase<List<Landmark>> {
    override operator fun invoke(): Flow<List<Landmark>> = repository.getLandmarks()
}

/**
 * Use case for getting active (not visited) landmarks.
 */
class GetActiveLandmarks @Inject constructor(
    private val repository: LandmarkRepository
) : FlowUseCase<List<Landmark>> {
    override operator fun invoke(): Flow<List<Landmark>> = repository.getActiveLandmarks()
}

/**
 * Use case for getting completed (visited) landmarks.
 */
class GetCompletedLandmarks @Inject constructor(
    private val repository: LandmarkRepository
) : FlowUseCase<List<Landmark>> {
    override operator fun invoke(): Flow<List<Landmark>> = repository.getCompletedLandmarks()
}

/**
 * Use case for getting a landmark by ID.
 */
class GetLandmarkById @Inject constructor(
    private val repository: LandmarkRepository
) : ResultUseCase<Landmark?, String> {
    override suspend operator fun invoke(params: String): Result<Landmark?> =
        Result.success(repository.getLandmarkById(params))
}

/**
 * Use case for marking a landmark as visited.
 */
class MarkLandmarkAsVisited @Inject constructor(
    private val repository: LandmarkRepository
) {
    suspend operator fun invoke(landmarkId: String): Result<Unit> {
        return try {
            repository.markAsVisited(landmarkId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}

/**
 * Use case for marking a landmark as not visited.
 */
class MarkLandmarkAsNotVisited @Inject constructor(
    private val repository: LandmarkRepository
) {
    suspend operator fun invoke(landmarkId: String): Result<Unit> {
        return try {
            repository.markAsNotVisited(landmarkId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}

/**
 * Use case for importing landmarks from GeoJSON.
 */
class ImportLandmarksUseCase @Inject constructor(
    private val repository: LandmarkRepository
) : ResultUseCaseNoParams<ImportResult> {
    override suspend operator fun invoke(): Result<ImportResult> =
        try {
            Result.success(repository.importFromGeoJson())
        } catch (e: Exception) {
            Result.error(e)
        }
}

/**
 * Use case for checking if user is near a landmark.
 */
class IsUserNearLandmark @Inject constructor(
    private val repository: LandmarkRepository
) {
    data class Params(
        val userLat: Double,
        val userLng: Double,
        val landmark: Landmark
    )

    operator fun invoke(params: Params): Boolean =
        repository.isUserNearLandmark(params.userLat, params.userLng, params.landmark)
}

/**
 * Use case for getting the total count of landmarks.
 */
class GetLandmarksCount @Inject constructor(
    private val repository: LandmarkRepository
) : ResultUseCaseNoParams<Int> {
    override suspend operator fun invoke(): Result<Int> =
        try {
            Result.success(repository.getLandmarksCount())
        } catch (e: Exception) {
            Result.error(e)
        }
}
