package com.example.rpg_map_pet.presentation.map.cluster

/**
 * Границы прямоугольной области для кластеризации.
 */
data class ClusterBounds(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double
) {
    /**
     * Проверить, находится ли точка внутри границ.
     */
    fun contains(lat: Double, lon: Double): Boolean {
        return lat in minLat..maxLat && lon in minLon..maxLon
    }
}
