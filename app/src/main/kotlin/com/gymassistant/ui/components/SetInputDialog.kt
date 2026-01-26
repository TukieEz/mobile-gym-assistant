package com.gymassistant.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SetInputDialog(
    exerciseName: String,
    lastWeight: Float? = null,
    lastReps: Int? = null,
    onDismiss: () -> Unit,
    onConfirm: (weight: Float, reps: Int) -> Unit
) {
    var weight by remember { mutableStateOf(lastWeight?.toString() ?: "50") }
    var reps by remember { mutableStateOf(lastReps?.toString() ?: "10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Добавить подход",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Weight input
                Text(
                    text = "Вес (кг)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = {
                        val current = weight.toFloatOrNull() ?: 0f
                        if (current >= 2.5f) weight = (current - 2.5f).toString()
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "-2.5 кг")
                    }
                    
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier = Modifier.width(100.dp),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    
                    IconButton(onClick = {
                        val current = weight.toFloatOrNull() ?: 0f
                        weight = (current + 2.5f).toString()
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "+2.5 кг")
                    }
                }
                
                // Quick weight buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    listOf(-5f, -2.5f, +2.5f, +5f).forEach { delta ->
                        FilledTonalButton(
                            onClick = {
                                val current = weight.toFloatOrNull() ?: 0f
                                val newWeight = (current + delta).coerceAtLeast(0f)
                                weight = if (newWeight == newWeight.toInt().toFloat()) {
                                    newWeight.toInt().toString()
                                } else {
                                    newWeight.toString()
                                }
                            }
                        ) {
                            Text(if (delta > 0) "+${delta.toInt()}" else delta.toInt().toString())
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Reps input
                Text(
                    text = "Повторения",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = {
                        val current = reps.toIntOrNull() ?: 0
                        if (current > 1) reps = (current - 1).toString()
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "-1")
                    }
                    
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.width(80.dp),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    IconButton(onClick = {
                        val current = reps.toIntOrNull() ?: 0
                        reps = (current + 1).toString()
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "+1")
                    }
                }
                
                // Quick reps buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    listOf(5, 8, 10, 12, 15).forEach { quickReps ->
                        FilledTonalButton(
                            onClick = { reps = quickReps.toString() }
                        ) {
                            Text(quickReps.toString())
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val w = weight.toFloatOrNull() ?: 0f
                    val r = reps.toIntOrNull() ?: 0
                    if (w > 0 && r > 0) {
                        onConfirm(w, r)
                    }
                }
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
