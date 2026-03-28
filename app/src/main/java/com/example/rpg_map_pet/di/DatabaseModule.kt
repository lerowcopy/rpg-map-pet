package com.example.rpg_map_pet.di

import android.content.Context
import androidx.room.Room
import com.example.rpg_map_pet.data.local.LandmarkDao
import com.example.rpg_map_pet.data.local.QuestDao
import com.example.rpg_map_pet.data.local.QuestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection module for database.
 * Provides Room database and DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQuestDatabase(
        @ApplicationContext context: Context
    ): QuestDatabase {
        return Room.databaseBuilder(
            context,
            QuestDatabase::class.java,
            "quest_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideQuestDao(database: QuestDatabase): QuestDao {
        return database.questDao()
    }

    @Provides
    @Singleton
    fun provideLandmarkDao(database: QuestDatabase): LandmarkDao {
        return database.landmarkDao()
    }
}
