package com.example.rpg_map_pet.presentation.map.cluster

import kotlin.math.*

/**
 * Утилиты для расчётов в алгоритме кластеризации.
 */
object ClusterCalculator {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Расчитать расстояние между двумя точками в метрах (формула Haversine).
     */
    fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Расчитать расстояние между двумя точками.
     */
    fun distanceBetween(point1: ClusterItem, point2: ClusterItem): Double {
        return distanceBetween(
            point1.point.latitude, point1.point.longitude,
            point2.point.latitude, point2.point.longitude
        )
    }

    /**
     * Расчитать средний центр списка точек.
     */
    fun calculateCenter(items: List<ClusterItem>): com.yandex.mapkit.geometry.Point {
        require(items.isNotEmpty()) { "Items list cannot be empty" }

        val sumLat = items.sumOf { it.point.latitude }
        val sumLon = items.sumOf { it.point.longitude }

        return com.yandex.mapkit.geometry.Point(
            sumLat / items.size,
            sumLon / items.size
        )
    }

    /**
     * Проверить, находятся ли две точки близко друг к другу.
     */
    fun arePointsClose(
        point1: ClusterItem,
        point2: ClusterItem,
        thresholdMeters: Double
    ): Boolean {
        return distanceBetween(point1, point2) <= thresholdMeters
    }
}
