package com.example.rpg_map_pet.domain.repository

import com.example.rpg_map_pet.domain.model.Quest
import kotlinx.coroutines.flow.Flow

interface QuestRepository {
    
    val allQuests: Flow<List<Quest>>
    
    val activeQuests: Flow<List<Quest>>
    
    suspend fun getQuestById(id: String): Quest?
    
    suspend fun saveQuest(quest: Quest)
    
    suspend fun saveQuests(quests: List<Quest>)
    
    suspend fun completeQuest(questId: String)
    
    suspend fun deleteQuest(questId: String)
    
    suspend fun deleteAllQuests()
}
