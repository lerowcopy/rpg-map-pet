package com.example.rpg_map_pet.di.quest

import com.example.rpg_map_pet.data.repository.QuestRepositoryImpl
import com.example.rpg_map_pet.domain.quest.*
import com.example.rpg_map_pet.domain.quest.QuestRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection module for quest feature.
 * Provides quest repository and use cases.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class QuestModule {

    @Binds
    @Singleton
    abstract fun bindQuestRepository(
        questRepositoryImpl: QuestRepositoryImpl
    ): QuestRepository

    companion object {
        @Provides
        @Singleton
        fun provideGetAllQuests(repository: QuestRepository): GetAllQuests {
            return GetAllQuests(repository)
        }

        @Provides
        @Singleton
        fun provideGetActiveQuests(repository: QuestRepository): GetActiveQuests {
            return GetActiveQuests(repository)
        }

        @Provides
        @Singleton
        fun provideGetCompletedQuests(repository: QuestRepository): GetCompletedQuests {
            return GetCompletedQuests(repository)
        }

        @Provides
        @Singleton
        fun provideGetQuestById(repository: QuestRepository): GetQuestById {
            return GetQuestById(repository)
        }

        @Provides
        @Singleton
        fun provideCreateQuest(repository: QuestRepository): CreateQuest {
            return CreateQuest(repository)
        }

        @Provides
        @Singleton
        fun provideUpdateQuest(repository: QuestRepository): UpdateQuest {
            return UpdateQuest(repository)
        }

        @Provides
        @Singleton
        fun provideCompleteQuest(repository: QuestRepository): CompleteQuest {
            return CompleteQuest(repository)
        }

        @Provides
        @Singleton
        fun provideDeleteQuest(repository: QuestRepository): DeleteQuest {
            return DeleteQuest(repository)
        }

        @Provides
        @Singleton
        fun provideGenerateQuestsForLocation(repository: QuestRepository): GenerateQuestsForLocation {
            return GenerateQuestsForLocation(repository)
        }
    }
}
