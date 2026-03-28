package com.example.rpg_map_pet.domain.model

data class Landmark(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 50f,
    val isVisited: Boolean = false,
    val visitedAt: Long? = null,
    val photos: List<String> = emptyList()
)
