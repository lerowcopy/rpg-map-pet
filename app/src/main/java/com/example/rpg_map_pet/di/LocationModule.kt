package com.example.rpg_map_pet.di

import android.content.Context
import com.example.rpg_map_pet.data.repository.LocationRepositoryImpl
import com.example.rpg_map_pet.domain.repository.LocationRepository
import com.example.rpg_map_pet.domain.usecase.GetCurrentLocation
import com.example.rpg_map_pet.domain.usecase.GetLocationUpdates
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
