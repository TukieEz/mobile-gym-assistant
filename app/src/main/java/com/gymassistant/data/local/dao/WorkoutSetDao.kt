package com.gymassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gymassistant.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {

    @Insert
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Query("""
        SELECT * FROM workout_sets 
        WHERE timestamp >= :startOfDay AND timestamp < :endOfDay
        ORDER BY timestamp DESC
    """)
    fun getSetsForDay(startOfDay: Long, endOfDay: Long): Flow<List<WorkoutSetEntity>>

    @Query("""
        SELECT * FROM workout_sets 
        WHERE exerciseId = :exerciseId
        ORDER BY timestamp DESC
    """)
    fun getSetsForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Query("""
        SELECT * FROM workout_sets 
        WHERE exerciseId = :exerciseId 
        AND timestamp >= :startTime AND timestamp <= :endTime
        ORDER BY timestamp ASC
    """)
    fun getSetsForExerciseInRange(
        exerciseId: Long,
        startTime: Long,
        endTime: Long
    ): Flow<List<WorkoutSetEntity>>

    @Query("SELECT MAX(weight) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getMaxWeightForExercise(exerciseId: Long): Float?

    @Query("DELETE FROM workout_sets WHERE id = :id")
    suspend fun deleteSet(id: Long)

    @Query("""
        SELECT DISTINCT date(timestamp / 1000, 'unixepoch', 'localtime') as workoutDate
        FROM workout_sets
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    fun getRecentWorkoutDates(limit: Int = 30): Flow<List<String>>
}
