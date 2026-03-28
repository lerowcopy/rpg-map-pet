package com.example.rpg_map_pet.di.landmark

import android.content.Context
import com.example.rpg_map_pet.data.local.GeoJsonImporter
import com.example.rpg_map_pet.data.repository.LandmarkRepositoryImpl
import com.example.rpg_map_pet.domain.landmark.GetActiveLandmarks
import com.example.rpg_map_pet.domain.landmark.GetCompletedLandmarks
import com.example.rpg_map_pet.domain.landmark.GetLandmarkById
import com.example.rpg_map_pet.domain.landmark.GetLandmarks
import com.example.rpg_map_pet.domain.landmark.GetLandmarksCount
import com.example.rpg_map_pet.domain.landmark.ImportLandmarksUseCase
import com.example.rpg_map_pet.domain.landmark.IsUserNearLandmark
import com.example.rpg_map_pet.domain.landmark.LandmarkRepository
import com.example.rpg_map_pet.domain.landmark.MarkLandmarkAsNotVisited
import com.example.rpg_map_pet.domain.landmark.MarkLandmarkAsVisited
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection module for landmark feature.
 * Provides landmark repository and use cases.
 */
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
        fun provideGetLandmarks(repository: LandmarkRepository): GetLandmarks {
            return GetLandmarks(repository)
        }

        @Provides
        @Singleton
        fun provideGetActiveLandmarks(repository: LandmarkRepository): GetActiveLandmarks {
            return GetActiveLandmarks(repository)
        }

        @Provides
        @Singleton
        fun provideGetCompletedLandmarks(repository: LandmarkRepository): GetCompletedLandmarks {
            return GetCompletedLandmarks(repository)
        }

        @Provides
        @Singleton
        fun provideGetLandmarkById(repository: LandmarkRepository): GetLandmarkById {
            return GetLandmarkById(repository)
        }

        @Provides
        @Singleton
        fun provideMarkLandmarkAsVisited(repository: LandmarkRepository): MarkLandmarkAsVisited {
            return MarkLandmarkAsVisited(repository)
        }

        @Provides
        @Singleton
        fun provideMarkLandmarkAsNotVisited(repository: LandmarkRepository): MarkLandmarkAsNotVisited {
            return MarkLandmarkAsNotVisited(repository)
        }

        @Provides
        @Singleton
        fun provideImportLandmarksUseCase(repository: LandmarkRepository): ImportLandmarksUseCase {
            return ImportLandmarksUseCase(repository)
        }

        @Provides
        @Singleton
        fun provideIsUserNearLandmark(repository: LandmarkRepository): IsUserNearLandmark {
            return IsUserNearLandmark(repository)
        }

        @Provides
        @Singleton
        fun provideGetLandmarksCount(repository: LandmarkRepository): GetLandmarksCount {
            return GetLandmarksCount(repository)
        }

        @Provides
        @Singleton
        fun provideGeoJsonImporter(
            @ApplicationContext context: Context
        ): GeoJsonImporter {
            return GeoJsonImporter(context)
        }
    }
}
