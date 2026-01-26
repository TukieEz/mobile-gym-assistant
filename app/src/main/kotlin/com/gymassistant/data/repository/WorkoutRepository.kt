package com.gymassistant.data.repository

import com.gymassistant.data.local.dao.ExerciseDao
import com.gymassistant.data.local.dao.WorkoutSessionDao
import com.gymassistant.data.local.dao.WorkoutSetDao
import com.gymassistant.data.local.entity.ExerciseEntity
import com.gymassistant.data.local.entity.SessionExerciseEntity
import com.gymassistant.data.local.entity.WorkoutSessionEntity
import com.gymassistant.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val sessionDao: WorkoutSessionDao,
    private val setDao: WorkoutSetDao
) {
    // Exercises
    fun getAllExercises(): Flow<List<ExerciseEntity>> = exerciseDao.getAllExercises()
    
    fun getExercisesByMuscleGroup(group: String): Flow<List<ExerciseEntity>> = 
        exerciseDao.getExercisesByMuscleGroup(group)
    
    suspend fun getExerciseById(id: Long): ExerciseEntity? = exerciseDao.getExerciseById(id)
    
    suspend fun insertExercise(exercise: ExerciseEntity): Long = exerciseDao.insertExercise(exercise)
    
    suspend fun insertExercises(exercises: List<ExerciseEntity>) = exerciseDao.insertExercises(exercises)

    // Sessions
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>> = sessionDao.getAllSessions()
    
    fun getActiveSession(): Flow<WorkoutSessionEntity?> = sessionDao.getActiveSession()
    
    suspend fun startNewSession(): Long {
        val session = WorkoutSessionEntity()
        return sessionDao.insertSession(session)
    }
    
    suspend fun finishSession(sessionId: Long) {
        sessionDao.finishSession(sessionId)
    }
    
    suspend fun deleteSession(sessionId: Long) = sessionDao.deleteSession(sessionId)

    // Session Exercises
    fun getSessionExercises(sessionId: Long): Flow<List<SessionExerciseEntity>> = 
        sessionDao.getSessionExercises(sessionId)
    
    suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long): Long {
        val order = sessionDao.getNextExerciseOrder(sessionId)
        val sessionExercise = SessionExerciseEntity(
            sessionId = sessionId,
            exerciseId = exerciseId,
            exerciseOrder = order
        )
        return sessionDao.insertSessionExercise(sessionExercise)
    }
    
    suspend fun removeExerciseFromSession(sessionExerciseId: Long) = 
        sessionDao.deleteSessionExercise(sessionExerciseId)

    // Sets
    fun getSetsForSessionExercise(sessionExerciseId: Long): Flow<List<WorkoutSetEntity>> = 
        setDao.getSetsForSessionExercise(sessionExerciseId)
    
    suspend fun addSet(
        sessionExerciseId: Long,
        weight: Float,
        reps: Int,
        rpe: Int? = null,
        rir: Int? = null,
        isWarmup: Boolean = false
    ): Long {
        val order = setDao.getNextSetOrder(sessionExerciseId)
        val set = WorkoutSetEntity(
            sessionExerciseId = sessionExerciseId,
            setOrder = order,
            weight = weight,
            reps = reps,
            rpe = rpe,
            rir = rir,
            isWarmup = isWarmup
        )
        return setDao.insertSet(set)
    }
    
    suspend fun updateSet(set: WorkoutSetEntity) = setDao.updateSet(set)
    
    suspend fun deleteSet(setId: Long) = setDao.deleteSet(setId)
    
    // Stats
    suspend fun getMaxWeightForExercise(exerciseId: Long): Float? = 
        setDao.getMaxWeightForExercise(exerciseId)
}
