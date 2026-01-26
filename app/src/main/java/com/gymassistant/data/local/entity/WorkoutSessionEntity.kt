package com.gymassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A workout session - contains multiple exercises with sets.
 * Based on Workout-Tracker Session entity.
 */
@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val note: String? = null
) {
    val isActive: Boolean get() = endTime == null
    
    val durationMinutes: Int? get() {
        return endTime?.let { ((it - startTime) / 60000).toInt() }
    }
}
