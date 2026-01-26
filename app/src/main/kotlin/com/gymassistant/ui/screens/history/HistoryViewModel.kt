package com.gymassistant.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymassistant.data.local.entity.WorkoutSessionEntity
import com.gymassistant.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repository: WorkoutRepository
) : ViewModel() {

    val sessions: StateFlow<List<WorkoutSessionEntity>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
