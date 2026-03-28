package com.example.rpg_map_pet.data.repository

import com.example.rpg_map_pet.data.local.QuestDao
import com.example.rpg_map_pet.data.local.QuestEntity
import com.example.rpg_map_pet.data.local.QuestType as EntityQuestType
import com.example.rpg_map_pet.domain.model.Quest
import com.example.rpg_map_pet.domain.model.QuestType as DomainQuestType
import com.example.rpg_map_pet.domain.quest.QuestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestRepositoryImpl @Inject constructor(
    private val questDao: QuestDao
) : QuestRepository {

    override val allQuests: Flow<List<Quest>> = questDao.getAllQuests().map { entities ->
        entities.map { it.toDomain() }
    }

    override val activeQuests: Flow<List<Quest>> = questDao.getActiveQuests().map { entities ->
        entities.map { it.toDomain() }
    }

    override val completedQuests: Flow<List<Quest>> = questDao.getCompletedQuests().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getQuestById(questId: String): Quest? {
        return questDao.getQuestById(questId)?.toDomain()
    }

    override suspend fun createQuest(quest: Quest) {
        questDao.insertQuest(quest.toEntity())
    }

    override suspend fun updateQuest(quest: Quest) {
        questDao.insertQuest(quest.toEntity())
    }

    override suspend fun completeQuest(questId: String) {
        val quest = questDao.getQuestById(questId) ?: return
        questDao.updateQuest(quest.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteQuest(questId: String) {
        questDao.deleteQuest(questId)
    }

    override suspend fun generateQuestsForLocation(latitude: Double, longitude: Double): List<Quest> {
        // TODO: Implement procedural quest generation based on location
        return emptyList()
    }

    // Mapper: Entity -> Domain
    private fun QuestEntity.toDomain(): Quest {
        return Quest(
            id = id,
            title = title,
            description = description,
            type = type.toDomain(),
            location = com.example.rpg_map_pet.domain.model.Location(latitude, longitude),
            radius = radius,
            isCompleted = isCompleted,
            createdAt = createdAt,
            completedAt = completedAt
        )
    }

    // Mapper: Domain -> Entity
    private fun Quest.toEntity(): QuestEntity {
        return QuestEntity(
            id = id,
            title = title,
            description = description,
            type = type.toEntity(),
            latitude = location.latitude,
            longitude = location.longitude,
            radius = radius,
            isCompleted = isCompleted,
            createdAt = createdAt,
            completedAt = completedAt
        )
    }

    private fun EntityQuestType.toDomain(): DomainQuestType {
        return when (this) {
            EntityQuestType.DELIVERY -> DomainQuestType.DELIVERY
            EntityQuestType.EXPLORATION -> DomainQuestType.EXPLORATION
            EntityQuestType.SCAN -> DomainQuestType.SCAN
            EntityQuestType.CHASE -> DomainQuestType.CHASE
            EntityQuestType.WAIT -> DomainQuestType.WAIT
        }
    }

    private fun DomainQuestType.toEntity(): EntityQuestType {
        return when (this) {
            DomainQuestType.DELIVERY -> EntityQuestType.DELIVERY
            DomainQuestType.EXPLORATION -> EntityQuestType.EXPLORATION
            DomainQuestType.SCAN -> EntityQuestType.SCAN
            DomainQuestType.CHASE -> EntityQuestType.CHASE
            DomainQuestType.WAIT -> EntityQuestType.WAIT
        }
    }
}
