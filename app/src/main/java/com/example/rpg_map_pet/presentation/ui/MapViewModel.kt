package com.example.rpg_map_pet.presentation.ui

import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rpg_map_pet.data.repository.LandmarkRepositoryImpl
import com.example.rpg_map_pet.domain.model.Landmark
import com.example.rpg_map_pet.domain.model.UserLocation
import com.example.rpg_map_pet.domain.usecase.GetCurrentLocation
import com.example.rpg_map_pet.domain.usecase.GetLocationUpdates
import com.example.rpg_map_pet.presentation.model.CameraPositionState
import com.example.rpg_map_pet.presentation.model.MapUiState
import com.example.rpg_map_pet.presentation.model.UserLocationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getCurrentLocation: GetCurrentLocation,
    private val getLocationUpdates: GetLocationUpdates,
    private val landmarkRepositoryImpl: LandmarkRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    private val _showEnableGpsDialog = MutableStateFlow(false)
    val showEnableGpsDialog: StateFlow<Boolean> = _showEnableGpsDialog.asStateFlow()
    
    private var landmarksLoaded = false

    companion object {
        private const val TAG = "MapViewModel"
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
                        ),
                        cameraPosition = CameraPositionState(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    )
                    
                    if (!landmarksLoaded) {
                        loadLandmarksNearUser(location.latitude, location.longitude)
                        landmarksLoaded = true
                    }
                    
                    checkLandmarkAchievements(location)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        userLocation = UserLocationState.Error(error.message ?: "Unknown error")
                    )
                }
            )
        }
    }
    
    private fun loadLandmarksNearUser(lat: Double, lng: Double) {
        viewModelScope.launch {
            landmarkRepositoryImpl.loadLandmarksNearby(lat, lng).fold(
                onSuccess = { landmarks ->
                    _uiState.value = _uiState.value.copy(landmarks = landmarks)
                },
                onFailure = { _ ->
                    _uiState.value = _uiState.value.copy(landmarks = emptyList())
                }
            )
        }
    }

    private fun checkLandmarkAchievements(location: UserLocation) {
        val currentState = _uiState.value
        currentState.landmarks.forEach { landmark ->
            if (!landmark.isVisited &&
                landmarkRepositoryImpl.isUserNearLandmark(location.latitude, location.longitude, landmark)) {
                viewModelScope.launch {
                    landmarkRepositoryImpl.markAsVisited(landmark.id)
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

    fun centerOnUserLocation() {
        val currentState = _uiState.value.userLocation
        if (currentState is UserLocationState.Success) {
            _uiState.value = _uiState.value.copy(
                cameraPosition = CameraPositionState(
                    latitude = currentState.latitude,
                    longitude = currentState.longitude
                )
            )
        }
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
}

fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}
