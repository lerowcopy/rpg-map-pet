package com.example.rpg_map_pet.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LandmarkPhotoDao {

    @Query("SELECT * FROM landmark_photos WHERE landmarkId = :landmarkId ORDER BY createdAt ASC")
    fun getPhotosByLandmarkId(landmarkId: String): Flow<List<LandmarkPhotoEntity>>

    @Query("SELECT * FROM landmark_photos WHERE landmarkId = :landmarkId ORDER BY createdAt ASC")
    suspend fun getPhotosByLandmarkIdSync(landmarkId: String): List<LandmarkPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: LandmarkPhotoEntity): Long

    @Query("DELETE FROM landmark_photos WHERE id = :photoId")
    suspend fun deletePhoto(photoId: Long)

    @Query("DELETE FROM landmark_photos WHERE landmarkId = :landmarkId")
    suspend fun deletePhotosByLandmarkId(landmarkId: String)
}
