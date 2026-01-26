package com.gymassistant.ui.screens.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymassistant.data.local.entity.ExerciseEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        floatingActionButton = {
            if (uiState.activeSession != null) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.showExercisePicker() },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Упражнение") }
                )
            }
        }
    ) { paddingValues ->
        if (uiState.activeSession == null) {
            // No active session - show start button
            NoActiveSessionContent(
                onStartWorkout = { viewModel.startWorkout() },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // Active session - show exercises
            ActiveSessionContent(
                uiState = uiState,
                onFinishWorkout = { viewModel.finishWorkout() },
                onAddSet = { sessionExerciseId, weight, reps ->
                    viewModel.addSet(sessionExerciseId, weight, reps)
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    // Exercise picker bottom sheet
    if (uiState.showExercisePicker) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideExercisePicker() },
            sheetState = sheetState
        ) {
            ExercisePickerContent(
                exercises = exercises,
                onExerciseSelected = { viewModel.addExercise(it.id) }
            )
        }
    }
}

@Composable
private fun NoActiveSessionContent(
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Готов к тренировке?",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Начни новую сессию",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStartWorkout,
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Начать тренировку", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ActiveSessionContent(
    uiState: WorkoutUiState,
    onFinishWorkout: () -> Unit,
    onAddSet: (Long, Float, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Тренировка",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.sessionExercises.size} упражнений",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onFinishWorkout) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Завершить")
            }
        }

        if (uiState.sessionExercises.isEmpty()) {
            // No exercises yet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Добавь первое упражнение",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Нажми + внизу экрана",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Exercise list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.sessionExercises) { item ->
                    ExerciseCard(
                        exerciseWithDetails = item,
                        onAddSet = onAddSet
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // FAB space
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exerciseWithDetails: SessionExerciseWithDetails,
    onAddSet: (Long, Float, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exerciseWithDetails.exercise.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = exerciseWithDetails.exercise.muscleGroup,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sets list
            if (exerciseWithDetails.sets.isEmpty()) {
                Text(
                    text = "Нет подходов",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                exerciseWithDetails.sets.forEachIndexed { index, set ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${index + 1}.")
                        Text("${set.weight} кг")
                        Text("${set.reps} повт")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Quick add button (simplified for MVP)
            Button(
                onClick = { onAddSet(exerciseWithDetails.sessionExercise.id, 50f, 10) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить подход")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerContent(
    exercises: List<ExerciseEntity>,
    onExerciseSelected: (ExerciseEntity) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Выбери упражнение",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (exercises.isEmpty()) {
            Text(
                text = "Нет упражнений. Добавь их в настройках.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exercises) { exercise ->
                    Card(
                        onClick = { onExerciseSelected(exercise) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = exercise.muscleGroup,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Добавить",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
