package com.example.rpg_map_pet.presentation.map

import com.example.rpg_map_pet.domain.model.Landmark
import com.yandex.mapkit.geometry.Point

/**
 * UI State for the map screen.
 * Represents all the UI data needed to render the map screen.
 */
data class MapUiState(
    val userLocation: UserLocationState = UserLocationState.Loading,
    val userLocationPoint: Point? = null,
    val cameraPosition: CameraPositionState? = null,
    val landmarks: List<Landmark> = emptyList(),
    val selectedLandmark: Landmark? = null,
    val showLandmarkInfo: Boolean = false
)

/**
 * Represents the state of user location in the UI.
 */
sealed class UserLocationState {
    object Loading : UserLocationState()
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float = 0f,
        val speed: Float = 0f
    ) : UserLocationState()
    data class Error(val message: String) : UserLocationState()
}

/**
 * Represents the camera position state for the map.
 */
data class CameraPositionState(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float = 17f,
    val azimuth: Float = 0f,
    val tilt: Float = 0f
)
