package com.example.rpg_map_pet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность точки интереса (landmark) из GeoJSON
 */
@Entity(tableName = "landmarks")
data class LandmarkEntity(
    @PrimaryKey val id: String,           // ID из OSM (node/123456)
    val name: String,                     // Название места
    val latitude: Double,                 // Широта
    val longitude: Double,                // Долгота
    val type: String,                     // Тип места (monument, memorial, attraction и т.д.)
    val description: String = "",         // Описание
    val isCompleted: Boolean = false,     // Посещено ли игроком
    val xpReward: Int = 100               // Награда в XP
)
