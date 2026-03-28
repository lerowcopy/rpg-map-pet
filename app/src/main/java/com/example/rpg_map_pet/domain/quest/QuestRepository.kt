package com.example.rpg_map_pet.domain.quest

import com.example.rpg_map_pet.domain.model.Quest
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for quest operations.
 * Part of the domain layer - defines the contract for quest data.
 */
interface QuestRepository {
    val allQuests: Flow<List<Quest>>
    val activeQuests: Flow<List<Quest>>
    val completedQuests: Flow<List<Quest>>
    suspend fun getQuestById(questId: String): Quest?
    suspend fun createQuest(quest: Quest)
    suspend fun updateQuest(quest: Quest)
    suspend fun completeQuest(questId: String)
    suspend fun deleteQuest(questId: String)
    suspend fun generateQuestsForLocation(latitude: Double, longitude: Double): List<Quest>
}
