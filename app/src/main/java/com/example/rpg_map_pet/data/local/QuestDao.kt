package com.example.rpg_map_pet.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {

    @Query("SELECT * FROM quests")
    fun getAllQuests(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE id = :questId")
    suspend fun getQuestById(questId: String): QuestEntity?

    @Query("SELECT * FROM quests WHERE isCompleted = 0")
    fun getActiveQuests(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE isCompleted = 1")
    fun getCompletedQuests(): Flow<List<QuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: QuestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<QuestEntity>)

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Query("DELETE FROM quests WHERE id = :questId")
    suspend fun deleteQuest(questId: String)

    @Query("DELETE FROM quests")
    suspend fun deleteAllQuests()
}
