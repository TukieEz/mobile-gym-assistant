package com.gymassistant.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymassistant.data.local.dao.PersonalRecordDao
import com.gymassistant.data.local.entity.ExerciseEntity
import com.gymassistant.data.local.entity.PersonalRecordEntity
import com.gymassistant.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordWithExercise(
    val record: PersonalRecordEntity,
    val exercise: ExerciseEntity?
)

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val personalRecordDao: PersonalRecordDao,
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _records = MutableStateFlow<List<RecordWithExercise>>(emptyList())
    val records: StateFlow<List<RecordWithExercise>> = _records.asStateFlow()

    init {
        loadRecords()
    }

    private fun loadRecords() {
        viewModelScope.launch {
            personalRecordDao.getRecentRecords(20).collect { recordList ->
                val recordsWithExercises = recordList.map { record ->
                    val exercise = repository.getExerciseById(record.exerciseId)
                    RecordWithExercise(record, exercise)
                }
                _records.value = recordsWithExercises
            }
        }
    }
}
