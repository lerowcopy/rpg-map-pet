package com.example.rpg_map_pet.presentation.map.cluster

import com.yandex.mapkit.geometry.Point

/**
 * Кластер — группа близких меток.
 */
data class Cluster(
    val center: Point,
    val items: List<ClusterItem>,
    val zoomLevel: Int
) {
    val size: Int get() = items.size

    /**
     * true, если кластер содержит только одну метку (не требует кластеризации).
     */
    val isSingle: Boolean get() = size == 1

    /**
     * true, если все метки в кластере посещённые.
     */
    val isAllVisited: Boolean get() = items.all { it.landmark.isVisited }

    /**
     * true, если некоторые метки в кластере посещённые.
     */
    val isPartiallyVisited: Boolean get() = items.any { it.landmark.isVisited } && !isAllVisited

    /**
     * Получить единственную метку, если кластер одиночный.
     */
    fun getSingleLandmark(): ClusterItem? {
        return if (isSingle) items.first() else null
    }
}
