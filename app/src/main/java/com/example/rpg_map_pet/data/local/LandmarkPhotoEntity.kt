package com.example.rpg_map_pet.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность фотографии метки
 */
@Entity(
    tableName = "landmark_photos",
    foreignKeys = [
        ForeignKey(
            entity = LandmarkEntity::class,
            parentColumns = ["id"],
            childColumns = ["landmarkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["landmarkId"])]
)
data class LandmarkPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val landmarkId: String,
    val photoPath: String,
    val createdAt: Long = System.currentTimeMillis()
)
