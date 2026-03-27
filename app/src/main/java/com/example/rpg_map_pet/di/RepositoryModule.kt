package com.example.rpg_map_pet.di

import com.example.rpg_map_pet.data.repository.QuestRepositoryImpl
import com.example.rpg_map_pet.domain.repository.QuestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindQuestRepository(
        impl: QuestRepositoryImpl
    ): QuestRepository
}
