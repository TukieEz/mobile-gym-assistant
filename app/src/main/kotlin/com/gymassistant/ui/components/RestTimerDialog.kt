package com.gymassistant.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun RestTimerDialog(
    initialSeconds: Int = 90,
    onDismiss: () -> Unit,
    onTimerFinished: () -> Unit
) {
    var totalSeconds by remember { mutableIntStateOf(initialSeconds) }
    var remainingSeconds by remember { mutableIntStateOf(initialSeconds) }
    var isRunning by remember { mutableStateOf(true) }
    
    val progress by animateFloatAsState(
        targetValue = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f,
        label = "timer_progress"
    )

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        } else if (remainingSeconds == 0 && isRunning) {
            onTimerFinished()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Отдых",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular progress with time
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(150.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(150.dp),
                        strokeWidth = 8.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = formatTime(remainingSeconds),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Time adjustment
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (remainingSeconds >= 15) {
                            remainingSeconds -= 15
                            totalSeconds -= 15
                        }
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "-15 сек")
                    }
                    
                    Text(
                        text = "±15 сек",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    IconButton(onClick = {
                        remainingSeconds += 15
                        totalSeconds += 15
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "+15 сек")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Play/Pause controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { isRunning = !isRunning },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Пауза" else "Продолжить",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    FilledIconButton(
                        onClick = {
                            remainingSeconds = totalSeconds
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Сброс",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Пропустить")
            }
        }
    )
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}
