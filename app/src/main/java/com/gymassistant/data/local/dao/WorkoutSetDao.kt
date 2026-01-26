package com.gymassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymassistant.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {

    @Query("SELECT * FROM workout_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY setOrder ASC")
    fun getSetsForSessionExercise(sessionExerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE id = :id")
    suspend fun getSetById(id: Long): WorkoutSetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE id = :id")
    suspend fun deleteSet(id: Long)

    @Query("SELECT COALESCE(MAX(setOrder), -1) + 1 FROM workout_sets WHERE sessionExerciseId = :sessionExerciseId")
    suspend fun getNextSetOrder(sessionExerciseId: Long): Int

    // Stats queries
    @Query("""
        SELECT MAX(weight) FROM workout_sets ws
        INNER JOIN session_exercises se ON ws.sessionExerciseId = se.id
        WHERE se.exerciseId = :exerciseId AND ws.isWarmup = 0
    """)
    suspend fun getMaxWeightForExercise(exerciseId: Long): Float?

    @SuppressWarnings("RoomWarnings.CURSOR_MISMATCH")
    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN session_exercises se ON ws.sessionExerciseId = se.id
        WHERE se.exerciseId = :exerciseId AND ws.isPersonalRecord = 1
        ORDER BY ws.timestamp DESC
        LIMIT 10
    """)
    fun getPersonalRecordsForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>>
}
