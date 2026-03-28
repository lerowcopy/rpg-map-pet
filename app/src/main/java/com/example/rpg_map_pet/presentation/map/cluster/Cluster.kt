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
     * Получить единственную метку, если кластер одиночный.
     */
    fun getSingleLandmark(): ClusterItem? {
        return if (isSingle) items.first() else null
    }
}
