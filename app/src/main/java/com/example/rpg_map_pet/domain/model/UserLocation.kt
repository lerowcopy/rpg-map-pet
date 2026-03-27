package com.example.rpg_map_pet.domain.model

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
