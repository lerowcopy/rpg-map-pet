package com.example.rpg_map_pet.di.location

import android.content.Context
import com.example.rpg_map_pet.data.repository.LocationRepositoryImpl
import com.example.rpg_map_pet.domain.location.LocationRepository
import com.example.rpg_map_pet.domain.location.GetCurrentLocation
import com.example.rpg_map_pet.domain.location.GetLocationUpdates
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection module for location feature.
 * Provides location repository and use cases.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository

    companion object {
        @Provides
        @Singleton
        fun provideGetLocationUpdates(repository: LocationRepository): GetLocationUpdates {
            return GetLocationUpdates(repository)
        }

        @Provides
        @Singleton
        fun provideGetCurrentLocation(repository: LocationRepository): GetCurrentLocation {
            return GetCurrentLocation(repository)
        }
    }
}
