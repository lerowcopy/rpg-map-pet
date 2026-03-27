package com.example.rpg_map_pet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность квеста в локальной базе данных
 */
@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: QuestType,
    val latitude: Double,
    val longitude: Double,
    val radius: Float, // радиус гео-фенсинга в метрах
    val isCompleted: Boolean,
    val createdAt: Long,
    val completedAt: Long?
)

enum class QuestType {
    DELIVERY,      // Доставка предмета
    EXPLORATION,   // Исследование локации
    SCAN,          // Сканирование объекта
    CHASE,         // Погоня
    WAIT           // Ожидание
}
