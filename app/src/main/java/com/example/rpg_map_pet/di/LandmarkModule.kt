package com.example.rpg_map_pet.di

import com.example.rpg_map_pet.data.repository.LandmarkRepositoryImpl
import com.example.rpg_map_pet.domain.repository.LandmarkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
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
}
