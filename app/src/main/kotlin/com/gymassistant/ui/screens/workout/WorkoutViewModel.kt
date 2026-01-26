package com.gymassistant.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymassistant.data.local.entity.ExerciseEntity
import com.gymassistant.data.local.entity.SessionExerciseEntity
import com.gymassistant.data.local.entity.WorkoutSessionEntity
import com.gymassistant.data.local.entity.WorkoutSetEntity
import com.gymassistant.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val isLoading: Boolean = true,
    val activeSession: WorkoutSessionEntity? = null,
    val sessionExercises: List<SessionExerciseWithDetails> = emptyList(),
    val showExercisePicker: Boolean = false,
    val currentWeight: String = "",
    val currentReps: String = ""
)

data class SessionExerciseWithDetails(
    val sessionExercise: SessionExerciseEntity,
    val exercise: ExerciseEntity,
    val sets: List<WorkoutSetEntity>
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    val exercises: StateFlow<List<ExerciseEntity>> = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        observeActiveSession()
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            repository.getActiveSession().collect { session ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activeSession = session
                )
                session?.let { loadSessionExercises(it.id) }
            }
        }
    }

    private fun loadSessionExercises(sessionId: Long) {
        viewModelScope.launch {
            repository.getSessionExercises(sessionId).collect { sessionExercises ->
                val details = sessionExercises.mapNotNull { se ->
                    val exercise = repository.getExerciseById(se.exerciseId)
                    exercise?.let {
                        SessionExerciseWithDetails(
                            sessionExercise = se,
                            exercise = it,
                            sets = emptyList() // TODO: load sets
                        )
                    }
                }
                _uiState.value = _uiState.value.copy(sessionExercises = details)
            }
        }
    }

    fun startWorkout() {
        viewModelScope.launch {
            repository.startNewSession()
        }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            _uiState.value.activeSession?.let { session ->
                repository.finishSession(session.id)
            }
        }
    }

    fun showExercisePicker() {
        _uiState.value = _uiState.value.copy(showExercisePicker = true)
    }

    fun hideExercisePicker() {
        _uiState.value = _uiState.value.copy(showExercisePicker = false)
    }

    fun addExercise(exerciseId: Long) {
        viewModelScope.launch {
            _uiState.value.activeSession?.let { session ->
                repository.addExerciseToSession(session.id, exerciseId)
            }
            hideExercisePicker()
        }
    }

    fun removeExercise(sessionExerciseId: Long) {
        viewModelScope.launch {
            repository.removeExerciseFromSession(sessionExerciseId)
        }
    }

    fun addSet(sessionExerciseId: Long, weight: Float, reps: Int) {
        viewModelScope.launch {
            repository.addSet(sessionExerciseId, weight, reps)
        }
    }

    fun updateWeight(value: String) {
        _uiState.value = _uiState.value.copy(currentWeight = value)
    }

    fun updateReps(value: String) {
        _uiState.value = _uiState.value.copy(currentReps = value)
    }
}
