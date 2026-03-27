package com.example.rpg_map_pet

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rpg_map_pet.data.worker.GeoJsonImportWorker
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MapPetApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        val apiKey = BuildConfig.YANDEX_MAPS_API_KEY

        Log.d("MapPetApp", "=== Yandex MapKit Debug ===")
        Log.d("MapPetApp", "API Key length: ${apiKey.length}")
        Log.d("MapPetApp", "API Key starts with: ${apiKey.take(8)}...")
        Log.d("MapPetApp", "Package name: $packageName")

        if (apiKey.isEmpty()) {
            Log.e("MapPetApp", "ERROR: Yandex Maps API key is empty!")
            Log.e("MapPetApp", "Add yandexMapsApiKey=YOUR_KEY to local.properties")
        } else {
            try {
                MapKitFactory.setApiKey(apiKey)
                MapKitFactory.initialize(this)
                Log.d("MapPetApp", "MapKit initialized successfully!")
            } catch (e: Exception) {
                Log.e("MapPetApp", "ERROR initializing MapKit: ${e.message}", e)
            }
        }

        // Планируем импорт GeoJSON при первом запуске
        scheduleGeoJsonImport()
    }

    private fun scheduleGeoJsonImport() {
        val importRequest = OneTimeWorkRequestBuilder<GeoJsonImportWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .setInitialDelay(2, TimeUnit.SECONDS) // Небольшая задержка после запуска
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "geojson_import",
            ExistingWorkPolicy.KEEP, // Только один раз
            importRequest
        )
    }
}
