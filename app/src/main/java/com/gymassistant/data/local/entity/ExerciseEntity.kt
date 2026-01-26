package com.gymassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Exercise definition.
 * Based on Workout-Tracker Exercise entity with muscle targeting.
 */
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val muscleGroup: String, // Primary muscle group
    val secondaryMuscles: String? = null, // Comma-separated secondary muscles
    val equipment: String? = null, // barbell, dumbbell, machine, bodyweight, cable
    val isCustom: Boolean = false,
    val isArchived: Boolean = false
)
