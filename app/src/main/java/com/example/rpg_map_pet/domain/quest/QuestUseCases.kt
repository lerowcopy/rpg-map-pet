package com.example.rpg_map_pet.domain.quest

import com.example.rpg_map_pet.core.base.FlowUseCase
import com.example.rpg_map_pet.core.base.ResultUseCase
import com.example.rpg_map_pet.core.base.ResultUseCaseNoParams
import com.example.rpg_map_pet.core.result.Result
import com.example.rpg_map_pet.domain.model.Quest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all quests.
 */
class GetAllQuests @Inject constructor(
    private val repository: QuestRepository
) : FlowUseCase<List<Quest>> {
    override operator fun invoke(): Flow<List<Quest>> = repository.allQuests
}

/**
 * Use case for getting active quests.
 */
class GetActiveQuests @Inject constructor(
    private val repository: QuestRepository
) : FlowUseCase<List<Quest>> {
    override operator fun invoke(): Flow<List<Quest>> = repository.activeQuests
}

/**
 * Use case for getting completed quests.
 */
class GetCompletedQuests @Inject constructor(
    private val repository: QuestRepository
) : FlowUseCase<List<Quest>> {
    override operator fun invoke(): Flow<List<Quest>> = repository.completedQuests
}

/**
 * Use case for getting a quest by ID.
 */
class GetQuestById @Inject constructor(
    private val repository: QuestRepository
) : ResultUseCase<Quest?, String> {
    override suspend operator fun invoke(params: String): Result<Quest?> =
        Result.success(repository.getQuestById(params))
}

/**
 * Use case for creating a new quest.
 */
class CreateQuest @Inject constructor(
    private val repository: QuestRepository
) {
    suspend operator fun invoke(quest: Quest): Result<Unit> {
        return try {
            repository.createQuest(quest)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}

/**
 * Use case for updating an existing quest.
 */
class UpdateQuest @Inject constructor(
    private val repository: QuestRepository
) {
    suspend operator fun invoke(quest: Quest): Result<Unit> {
        return try {
            repository.updateQuest(quest)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}

/**
 * Use case for completing a quest.
 */
class CompleteQuest @Inject constructor(
    private val repository: QuestRepository
) {
    suspend operator fun invoke(questId: String): Result<Unit> {
        return try {
            repository.completeQuest(questId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}

/**
 * Use case for deleting a quest.
 */
class DeleteQuest @Inject constructor(
    private val repository: QuestRepository
) {
    suspend operator fun invoke(questId: String): Result<Unit> {
        return try {
            repository.deleteQuest(questId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}

/**
 * Use case for generating quests based on location.
 */
class GenerateQuestsForLocation @Inject constructor(
    private val repository: QuestRepository
) : ResultUseCase<List<Quest>, GenerateQuestsForLocation.Params> {
    
    data class Params(
        val latitude: Double,
        val longitude: Double
    )
    
    override suspend operator fun invoke(params: Params): Result<List<Quest>> =
        try {
            Result.success(repository.generateQuestsForLocation(params.latitude, params.longitude))
        } catch (e: Exception) {
            Result.error(e)
        }
}
