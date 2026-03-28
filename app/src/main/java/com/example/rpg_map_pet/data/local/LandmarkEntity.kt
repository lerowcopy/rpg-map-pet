package com.example.rpg_map_pet.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.rpg_map_pet.domain.model.Landmark

/**
 * Сущность точки интереса (landmark) из GeoJSON
 */
@Entity(
    tableName = "landmarks",
    indices = [
        Index(value = ["latitude"]),
        Index(value = ["longitude"]),
        Index(value = ["latitude", "longitude"])
    ]
)
data class LandmarkEntity(
    @PrimaryKey val id: String,           // ID из OSM (node/123456)
    val name: String,                     // Название места
    val latitude: Double,                 // Широта
    val longitude: Double,                // Долгота
    val type: String,                     // Тип места (monument, memorial, attraction и т.д.)
    val description: String = "",         // Описание
    val isCompleted: Boolean = false,     // Посещено ли игроком
    val completedAt: Long? = null,        // Время посещения (timestamp)
    val xpReward: Int = 100               // Награда в XP
)

fun LandmarkEntity.toDomainModel(photos: List<String> = emptyList()): Landmark {
    return Landmark(
        id = id,
        name = name,
        description = description,
        latitude = latitude,
        longitude = longitude,
        radius = 50f,
        isVisited = isCompleted,
        visitedAt = completedAt,
        photos = photos
    )
}

fun Landmark.toEntity(): LandmarkEntity {
    return LandmarkEntity(
        id = id,
        name = name,
        description = description,
        latitude = latitude,
        longitude = longitude,
        type = "monument",
        isCompleted = isVisited,
        completedAt = visitedAt,
        xpReward = 100
    )
}
