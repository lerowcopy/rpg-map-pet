package com.example.rpg_map_pet.presentation.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rpg_map_pet.domain.model.Quest
import com.example.rpg_map_pet.domain.quest.CompleteQuest
import com.example.rpg_map_pet.domain.quest.DeleteQuest
import com.example.rpg_map_pet.domain.quest.GetActiveQuests
import com.example.rpg_map_pet.domain.quest.GetAllQuests
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the quest list screen.
 */
data class QuestUiState(
    val quests: List<Quest> = emptyList(),
    val activeQuests: List<Quest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the quest list screen.
 * Handles quest display and user interactions with quests.
 */
@HiltViewModel
class QuestViewModel @Inject constructor(
    getAllQuests: GetAllQuests,
    getActiveQuests: GetActiveQuests,
    private val completeQuest: CompleteQuest,
    private val deleteQuest: DeleteQuest
) : ViewModel() {

    val quests: StateFlow<List<Quest>> = getAllQuests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeQuests: StateFlow<List<Quest>> = getActiveQuests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun completeQuest(questId: String) {
        viewModelScope.launch {
            completeQuest(questId)
        }
    }

    fun deleteQuest(questId: String) {
        viewModelScope.launch {
            deleteQuest(questId)
        }
    }
}
