package com.example.rpg_map_pet.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rpg_map_pet.data.local.GeoJsonImporter
import com.example.rpg_map_pet.data.local.LandmarkDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker для импорта GeoJSON данных при первом запуске приложения
 */
@HiltWorker
class GeoJsonImportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val landmarkDao: LandmarkDao,
    private val geoJsonImporter: GeoJsonImporter
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val result = geoJsonImporter.importToDatabase(landmarkDao)
            when (result) {
                is GeoJsonImporter.ImportResult.Success -> {
                    android.util.Log.d("GeoJsonImport", "Imported ${result.count} landmarks")
                    Result.success()
                }
                is GeoJsonImporter.ImportResult.AlreadyImported -> {
                    android.util.Log.d("GeoJsonImport", "Already imported (${result.count} landmarks)")
                    Result.success()
                }
                is GeoJsonImporter.ImportResult.Error -> {
                    android.util.Log.e("GeoJsonImport", "Import error: ${result.message}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeoJsonImport", "Exception during import: ${e.message}", e)
            Result.retry()
        }
    }
}
