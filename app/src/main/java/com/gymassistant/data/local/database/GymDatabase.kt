package com.gymassistant.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gymassistant.data.local.dao.ExerciseDao
import com.gymassistant.data.local.dao.PersonalRecordDao
import com.gymassistant.data.local.dao.WorkoutSessionDao
import com.gymassistant.data.local.dao.WorkoutSetDao
import com.gymassistant.data.local.entity.ExerciseEntity
import com.gymassistant.data.local.entity.PersonalRecordEntity
import com.gymassistant.data.local.entity.SessionExerciseEntity
import com.gymassistant.data.local.entity.WorkoutSessionEntity
import com.gymassistant.data.local.entity.WorkoutSetEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        SessionExerciseEntity::class,
        WorkoutSetEntity::class,
        PersonalRecordEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class GymDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun workoutSetDao(): WorkoutSetDao
    abstract fun personalRecordDao(): PersonalRecordDao
}
