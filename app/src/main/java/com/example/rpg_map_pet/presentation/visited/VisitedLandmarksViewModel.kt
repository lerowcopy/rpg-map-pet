package com.example.rpg_map_pet.presentation.visited

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rpg_map_pet.domain.landmark.GetCompletedLandmarks
import com.example.rpg_map_pet.domain.landmark.MarkLandmarkAsNotVisited
import com.example.rpg_map_pet.domain.model.Landmark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State для экрана посещённых меток.
 */
data class VisitedLandmarksUiState(
    val visitedLandmarks: List<Landmark> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel для экрана посещённых меток.
 */
@HiltViewModel
class VisitedLandmarksViewModel @Inject constructor(
    private val getCompletedLandmarks: GetCompletedLandmarks,
    private val markLandmarkAsNotVisited: MarkLandmarkAsNotVisited
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisitedLandmarksUiState())
    val uiState: StateFlow<VisitedLandmarksUiState> = _uiState.asStateFlow()

    init {
        observeVisitedLandmarks()
    }

    private fun observeVisitedLandmarks() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        getCompletedLandmarks()
            .onEach { landmarks ->
                _uiState.value = _uiState.value.copy(
                    visitedLandmarks = landmarks,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun markAsNotVisited(landmarkId: String) {
        viewModelScope.launch {
            markLandmarkAsNotVisited(landmarkId)
        }
    }
}
