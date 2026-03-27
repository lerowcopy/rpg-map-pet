package com.example.rpg_map_pet.di

import android.content.Context
import com.example.rpg_map_pet.data.local.GeoJsonImporter
import com.example.rpg_map_pet.data.local.LandmarkDao
import com.example.rpg_map_pet.data.local.QuestDatabase
import com.example.rpg_map_pet.data.repository.LandmarkRepositoryImpl
import com.example.rpg_map_pet.domain.repository.LandmarkRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LandmarkModule {

    @Binds
    @Singleton
    abstract fun bindLandmarkRepository(
        landmarkRepositoryImpl: LandmarkRepositoryImpl
    ): LandmarkRepository

    companion object {
        @Provides
        @Singleton
        fun provideLandmarkDao(database: QuestDatabase): LandmarkDao {
            return database.landmarkDao()
        }

        @Provides
        @Singleton
        fun provideGeoJsonImporter(@ApplicationContext context: Context): GeoJsonImporter {
            return GeoJsonImporter(context)
        }
    }
}
