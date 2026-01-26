package com.gymassistant.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymassistant.data.local.entity.ExerciseEntity
import com.gymassistant.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseProgress(
    val exercise: ExerciseEntity,
    val maxWeight: Float?,
    val totalSets: Int = 0
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    val exercises: StateFlow<List<ExerciseEntity>> = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _progressData = MutableStateFlow<List<ExerciseProgress>>(emptyList())
    val progressData: StateFlow<List<ExerciseProgress>> = _progressData.asStateFlow()

    private val _selectedExercise = MutableStateFlow<ExerciseEntity?>(null)
    val selectedExercise: StateFlow<ExerciseEntity?> = _selectedExercise.asStateFlow()

    init {
        loadProgressData()
    }

    private fun loadProgressData() {
        viewModelScope.launch {
            exercises.collect { exerciseList ->
                val progress = exerciseList.map { exercise ->
                    val maxWeight = repository.getMaxWeightForExercise(exercise.id)
                    ExerciseProgress(
                        exercise = exercise,
                        maxWeight = maxWeight
                    )
                }.filter { it.maxWeight != null }
                    .sortedByDescending { it.maxWeight }
                
                _progressData.value = progress
            }
        }
    }

    fun selectExercise(exercise: ExerciseEntity?) {
        _selectedExercise.value = exercise
    }
}
