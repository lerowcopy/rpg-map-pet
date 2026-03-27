package com.example.rpg_map_pet.presentation.model

import com.example.rpg_map_pet.domain.model.Landmark

data class MapUiState(
    val userLocation: UserLocationState = UserLocationState.Loading,
    val cameraPosition: CameraPositionState? = null,
    val landmarks: List<Landmark> = emptyList(),
    val selectedLandmark: Landmark? = null,
    val showLandmarkInfo: Boolean = false
)

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

data class CameraPositionState(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float = 17f,
    val azimuth: Float = 0f,
    val tilt: Float = 0f
)
