package com.example.rpg_map_pet.domain.usecase

import com.example.rpg_map_pet.data.local.GeoJsonImporter
import com.example.rpg_map_pet.domain.repository.LandmarkRepository
import javax.inject.Inject

/**
 * UseCase для импорта данных из GeoJSON файла
 */
class ImportLandmarksUseCase @Inject constructor(
    private val landmarkRepository: LandmarkRepository
) {
    suspend operator fun invoke(): GeoJsonImporter.ImportResult {
        return landmarkRepository.importFromGeoJson()
    }
}
