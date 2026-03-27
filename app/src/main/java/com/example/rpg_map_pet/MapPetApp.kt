package com.example.rpg_map_pet

import android.app.Application
import android.util.Log
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MapPetApp : Application() {

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
    }
}
