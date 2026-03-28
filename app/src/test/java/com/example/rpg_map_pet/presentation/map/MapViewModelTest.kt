package com.example.rpg_map_pet.presentation.map

import com.example.rpg_map_pet.domain.model.Landmark
import com.example.rpg_map_pet.domain.model.UserLocation
import org.junit.Assert.*
import org.junit.Test

class MapViewModelTest {

    @Test
    fun `distance calculation test`() {
        // Координаты двух точек (примерно 100м друг от друга)
        val lat1 = 55.7520
        val lng1 = 37.6175
        val lat2 = 55.7529
        val lng2 = 37.6175

        val viewModel = createViewModel()
        val distance = viewModel.calculateDistance(lat1, lng1, lat2, lng2)

        // Расстояние должно быть около 100 метров
        assertTrue("Distance should be around 100m, but was $distance", 
            distance in 90f..110f)
    }

    @Test
    fun `isUserNearLandmark test - user is near`() {
        val userLocation = UserLocation(
            latitude = 55.7520,
            longitude = 37.6175,
            accuracy = 10f
        )

        val landmark = Landmark(
            id = "test_1",
            name = "Test Landmark",
            description = "Test",
            latitude = 55.7521, // ~11 метров
            longitude = 37.6175,
            radius = 50f
        )

        val viewModel = createViewModel()
        val distance = viewModel.calculateDistance(
            userLocation.latitude,
            userLocation.longitude,
            landmark.latitude,
            landmark.longitude
        )

        assertTrue("User should be near landmark (distance: ${distance}m)", 
            distance <= landmark.radius)
    }

    @Test
    fun `isUserNearLandmark test - user is far`() {
        val userLocation = UserLocation(
            latitude = 55.7520,
            longitude = 37.6175,
            accuracy = 10f
        )

        val landmark = Landmark(
            id = "test_2",
            name = "Test Landmark",
            description = "Test",
            latitude = 55.7600, // ~1 км
            longitude = 37.6175,
            radius = 50f
        )

        val viewModel = createViewModel()
        val distance = viewModel.calculateDistance(
            userLocation.latitude,
            userLocation.longitude,
            landmark.latitude,
            landmark.longitude
        )

        assertFalse("User should NOT be near landmark (distance: ${distance}m)", 
            distance <= landmark.radius)
    }
}

// Helper function для создания ViewModel в тестах
private fun createViewModel(): MapViewModel {
    // В реальном тесте нужно использовать MockK для моков
    return MapViewModel(
        getCurrentLocation = null!!,
        getLocationUpdates = null!!,
        getLandmarks = null!!,
        getLandmarksInBoundingBox = null!!,
        getLandmarksInRadius = null!!,
        markLandmarkAsVisited = null!!
    )
}
