package com.example.rpg_map_pet.presentation.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rpg_map_pet.core.result.Result
import com.example.rpg_map_pet.domain.landmark.GetLandmarks
import com.example.rpg_map_pet.domain.landmark.MarkLandmarkAsVisited
import com.example.rpg_map_pet.domain.location.GetCurrentLocation
import com.example.rpg_map_pet.domain.location.GetLocationUpdates
import com.example.rpg_map_pet.domain.model.Landmark
import com.example.rpg_map_pet.domain.model.UserLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the map screen.
 * Handles user location tracking, landmark display, and visit tracking.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val getCurrentLocation: GetCurrentLocation,
    private val getLocationUpdates: GetLocationUpdates,
    private val getLandmarks: GetLandmarks,
    private val markLandmarkAsVisited: MarkLandmarkAsVisited
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _showEnableGpsDialog = MutableStateFlow(false)
    val showEnableGpsDialog: StateFlow<Boolean> = _showEnableGpsDialog.asStateFlow()

    // Сохраняем позицию камеры
    private var savedCameraPosition: CameraPositionState? = null

    fun saveCameraPosition(latitude: Double, longitude: Double, zoom: Float) {
        savedCameraPosition = CameraPositionState(latitude, longitude, zoom)
    }

    fun restoreCameraPosition(): CameraPositionState? {
        return savedCameraPosition
    }

    companion object {
        private const val TAG = "MapViewModel"
    }

    init {
        observeLandmarks()
        // Инициализация локации при создании ViewModel
        loadUserLocation()
        startLocationUpdates()
    }

    private fun observeLandmarks() {
        getLandmarks()
            .onEach { landmarks ->
                _uiState.value = _uiState.value.copy(landmarks = landmarks)
            }
            .launchIn(viewModelScope)
    }

    fun loadUserLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(userLocation = UserLocationState.Loading)

            getCurrentLocation().fold(
                onSuccess = { location ->
                    _uiState.value = _uiState.value.copy(
                        userLocation = UserLocationState.Success(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy,
                            speed = location.speed
                        )
                        // Не меняем cameraPosition здесь - это вызывает сброс камеры
                    )

                    checkLandmarkAchievements(location)
                },
                onError = { error, message ->
                    _uiState.value = _uiState.value.copy(
                        userLocation = UserLocationState.Error(message ?: "Unknown error")
                    )
                }
            )
        }
    }

    private fun checkLandmarkAchievements(location: UserLocation) {
        viewModelScope.launch {
            val currentState = _uiState.value
            currentState.landmarks.forEach { landmark ->
                if (!landmark.isVisited &&
                    isUserNearLandmark(location, landmark)) {
                    markLandmarkAsVisited(landmark.id)
                }
            }
        }
    }

    fun startLocationUpdates() {
        viewModelScope.launch {
            getLocationUpdates().collect { location ->
                _uiState.value = _uiState.value.copy(
                    userLocation = UserLocationState.Success(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        speed = location.speed
                    )
                )
                checkLandmarkAchievements(location)
            }
        }
    }

    fun updateCameraPosition(latitude: Double, longitude: Double, zoom: Float = 14f) {
        _uiState.value = _uiState.value.copy(
            cameraPosition = CameraPositionState(
                latitude = latitude,
                longitude = longitude,
                zoom = zoom
            )
        )
    }

    fun selectLandmark(landmark: Landmark) {
        Log.d(TAG, "selectLandmark called: ${landmark.name}, showLandmarkInfo = true")
        _uiState.value = _uiState.value.copy(
            selectedLandmark = landmark,
            showLandmarkInfo = true
        )
        Log.d(TAG, "State updated: showLandmarkInfo = ${_uiState.value.showLandmarkInfo}")
    }

    fun dismissLandmarkInfo() {
        _uiState.value = _uiState.value.copy(
            showLandmarkInfo = false,
            selectedLandmark = null
        )
    }

    fun markAsVisited(landmarkId: String) {
        viewModelScope.launch {
            markLandmarkAsVisited(landmarkId)
        }

        val currentLandmarks = _uiState.value.landmarks
        val updatedLandmarks = currentLandmarks.map { landmark ->
            if (landmark.id == landmarkId) {
                landmark.copy(isVisited = true, visitedAt = System.currentTimeMillis())
            } else {
                landmark
            }
        }
        _uiState.value = _uiState.value.copy(
            landmarks = updatedLandmarks,
            selectedLandmark = updatedLandmarks.find { it.id == landmarkId },
            showLandmarkInfo = false
        )
    }

    fun showEnableGpsDialog() {
        _showEnableGpsDialog.value = true
    }

    fun hideEnableGpsDialog() {
        _showEnableGpsDialog.value = false
    }

    private fun isUserNearLandmark(userLocation: UserLocation, landmark: Landmark): Boolean {
        val distance = calculateDistance(
            userLocation.latitude,
            userLocation.longitude,
            landmark.latitude,
            landmark.longitude
        )
        return distance <= landmark.radius
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val earthRadius = 6371000

        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return (earthRadius * c).toFloat()
    }
}
