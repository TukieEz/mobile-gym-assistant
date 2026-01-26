package com.gymassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymassistant.data.local.entity.SessionExerciseEntity
import com.gymassistant.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    // Sessions
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE endTime IS NULL LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkoutSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("UPDATE workout_sessions SET endTime = :endTime WHERE id = :sessionId")
    suspend fun finishSession(sessionId: Long, endTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    // Session Exercises
    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY exerciseOrder ASC")
    fun getSessionExercises(sessionId: Long): Flow<List<SessionExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExercise(sessionExercise: SessionExerciseEntity): Long

    @Query("DELETE FROM session_exercises WHERE id = :id")
    suspend fun deleteSessionExercise(id: Long)

    @Query("SELECT COALESCE(MAX(exerciseOrder), -1) + 1 FROM session_exercises WHERE sessionId = :sessionId")
    suspend fun getNextExerciseOrder(sessionId: Long): Int
}
