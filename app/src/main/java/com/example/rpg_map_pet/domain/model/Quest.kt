package com.example.rpg_map_pet.domain.model

/**
 * Бизнес-модель квеста
 */
data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val type: QuestType,
    val location: Location,
    val radius: Float,
    val isCompleted: Boolean,
    val createdAt: Long,
    val completedAt: Long?
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

enum class QuestType {
    DELIVERY,      // Доставка предмета
    EXPLORATION,   // Исследование локации
    SCAN,          // Сканирование объекта
    CHASE,         // Погоня
    WAIT           // Ожидание
}
