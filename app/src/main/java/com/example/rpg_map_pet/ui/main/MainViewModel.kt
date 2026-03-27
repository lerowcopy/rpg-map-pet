package com.example.rpg_map_pet.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rpg_map_pet.domain.model.Quest
import com.example.rpg_map_pet.domain.repository.QuestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val questRepository: QuestRepository
) : ViewModel() {
    
    val quests: StateFlow<List<Quest>> = questRepository.allQuests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val activeQuests: StateFlow<List<Quest>> = questRepository.activeQuests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun completeQuest(questId: String) {
        viewModelScope.launch {
            questRepository.completeQuest(questId)
        }
    }
    
    fun deleteQuest(questId: String) {
        viewModelScope.launch {
            questRepository.deleteQuest(questId)
        }
    }
}
