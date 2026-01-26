package com.gymassistant.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single set within a session exercise.
 * Based on Workout-Tracker GymSet + MyFit RIR concept.
 */
@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = SessionExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionExerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionExerciseId: Long,
    val setOrder: Int = 0,
    val weight: Float, // in kg
    val reps: Int,
    val rpe: Int? = null, // Rate of Perceived Exertion (6-10 scale)
    val rir: Int? = null, // Reps In Reserve (0-5)
    val isWarmup: Boolean = false,
    val isDropSet: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isPersonalRecord: Boolean = false
)
