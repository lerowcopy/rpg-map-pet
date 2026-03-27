package com.example.rpg_map_pet.di

import android.content.Context
import com.example.rpg_map_pet.data.local.QuestDao
import com.example.rpg_map_pet.data.local.QuestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQuestDatabase(
        @ApplicationContext context: Context
    ): QuestDatabase {
        return QuestDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideQuestDao(database: QuestDatabase): QuestDao {
        return database.questDao()
    }
}
