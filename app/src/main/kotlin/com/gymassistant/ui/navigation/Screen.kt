package com.gymassistant.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Workout : Screen(
        route = "workout",
        title = "Тренировка",
        icon = Icons.Filled.FitnessCenter
    )
    
    object History : Screen(
        route = "history",
        title = "История",
        icon = Icons.Filled.History
    )
    
    object Progress : Screen(
        route = "progress",
        title = "Прогресс",
        icon = Icons.Filled.ShowChart
    )
    
    object Records : Screen(
        route = "records",
        title = "Рекорды",
        icon = Icons.Filled.EmojiEvents
    )
    
    companion object {
        val items = listOf(Workout, History, Progress, Records)
    }
}
