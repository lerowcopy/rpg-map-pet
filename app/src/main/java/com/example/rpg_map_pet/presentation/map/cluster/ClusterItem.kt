package com.example.rpg_map_pet.presentation.map.cluster

import com.example.rpg_map_pet.domain.model.Landmark
import com.yandex.mapkit.geometry.Point

/**
 * Элемент для кластеризации — обёртка над меткой.
 */
data class ClusterItem(
    val id: String,
    val point: Point,
    val landmark: Landmark
) {
    constructor(landmark: Landmark) : this(
        id = landmark.id,
        point = Point(landmark.latitude, landmark.longitude),
        landmark = landmark
    )
}
