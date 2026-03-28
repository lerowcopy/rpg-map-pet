package com.example.rpg_map_pet.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.example.rpg_map_pet.core.result.Result
import com.example.rpg_map_pet.core.result.toDomainResult
import com.example.rpg_map_pet.domain.location.LocationRepository
import com.example.rpg_map_pet.domain.model.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(): Flow<UserLocation> = callbackFlow {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location.toUserLocation())
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            android.os.Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<UserLocation> {
        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                Result.success(location.toUserLocation())
            } else {
                Result.failure("Location is null")
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    private fun Location.toUserLocation(): UserLocation {
        return UserLocation(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            speed = speed,
            timestamp = time
        )
    }
}
