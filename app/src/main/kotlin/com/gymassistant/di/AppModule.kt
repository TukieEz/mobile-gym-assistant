package com.gymassistant.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gymassistant.data.local.dao.ExerciseDao
import com.gymassistant.data.local.dao.WorkoutSessionDao
import com.gymassistant.data.local.dao.WorkoutSetDao
import com.gymassistant.data.local.database.GymDatabase
import com.gymassistant.data.local.entity.ExerciseEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGymDatabase(
        app: Application,
        exerciseDaoProvider: Provider<ExerciseDao>
    ): GymDatabase {
        return Room.databaseBuilder(
            app,
            GymDatabase::class.java,
            "gym_assistant.db"
        )
        .fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    exerciseDaoProvider.get().insertExercises(getDefaultExercises())
                }
            }
        })
        .build()
    }

    @Provides
    @Singleton
    fun provideExerciseDao(db: GymDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    @Singleton
    fun provideWorkoutSessionDao(db: GymDatabase): WorkoutSessionDao = db.workoutSessionDao()

    @Provides
    @Singleton
    fun provideWorkoutSetDao(db: GymDatabase): WorkoutSetDao = db.workoutSetDao()

    @Provides
    @Singleton
    fun providePersonalRecordDao(db: GymDatabase): com.gymassistant.data.local.dao.PersonalRecordDao = 
        db.personalRecordDao()
}

private fun getDefaultExercises(): List<ExerciseEntity> = listOf(
    // Chest
    ExerciseEntity(name = "Жим штанги лёжа", muscleGroup = "CHEST", equipment = "barbell"),
    ExerciseEntity(name = "Жим гантелей лёжа", muscleGroup = "CHEST", equipment = "dumbbell"),
    ExerciseEntity(name = "Жим на наклонной скамье", muscleGroup = "CHEST", equipment = "barbell"),
    ExerciseEntity(name = "Разводка гантелей", muscleGroup = "CHEST", equipment = "dumbbell"),
    ExerciseEntity(name = "Отжимания на брусьях", muscleGroup = "CHEST", equipment = "bodyweight"),
    
    // Back
    ExerciseEntity(name = "Становая тяга", muscleGroup = "BACK", equipment = "barbell"),
    ExerciseEntity(name = "Подтягивания", muscleGroup = "BACK", equipment = "bodyweight"),
    ExerciseEntity(name = "Тяга штанги в наклоне", muscleGroup = "BACK", equipment = "barbell"),
    ExerciseEntity(name = "Тяга гантели в наклоне", muscleGroup = "BACK", equipment = "dumbbell"),
    ExerciseEntity(name = "Тяга верхнего блока", muscleGroup = "BACK", equipment = "cable"),
    
    // Shoulders
    ExerciseEntity(name = "Жим штанги стоя", muscleGroup = "SHOULDERS", equipment = "barbell"),
    ExerciseEntity(name = "Жим гантелей сидя", muscleGroup = "SHOULDERS", equipment = "dumbbell"),
    ExerciseEntity(name = "Махи гантелями в стороны", muscleGroup = "SHOULDERS", equipment = "dumbbell"),
    ExerciseEntity(name = "Тяга штанги к подбородку", muscleGroup = "SHOULDERS", equipment = "barbell"),
    
    // Biceps
    ExerciseEntity(name = "Подъём штанги на бицепс", muscleGroup = "BICEPS", equipment = "barbell"),
    ExerciseEntity(name = "Подъём гантелей на бицепс", muscleGroup = "BICEPS", equipment = "dumbbell"),
    ExerciseEntity(name = "Молотки", muscleGroup = "BICEPS", equipment = "dumbbell"),
    
    // Triceps
    ExerciseEntity(name = "Французский жим", muscleGroup = "TRICEPS", equipment = "barbell"),
    ExerciseEntity(name = "Разгибания на блоке", muscleGroup = "TRICEPS", equipment = "cable"),
    ExerciseEntity(name = "Отжимания узким хватом", muscleGroup = "TRICEPS", equipment = "bodyweight"),
    
    // Legs
    ExerciseEntity(name = "Приседания со штангой", muscleGroup = "QUADS", equipment = "barbell"),
    ExerciseEntity(name = "Жим ногами", muscleGroup = "QUADS", equipment = "machine"),
    ExerciseEntity(name = "Выпады", muscleGroup = "QUADS", equipment = "dumbbell"),
    ExerciseEntity(name = "Разгибания ног", muscleGroup = "QUADS", equipment = "machine"),
    ExerciseEntity(name = "Сгибания ног", muscleGroup = "HAMSTRINGS", equipment = "machine"),
    ExerciseEntity(name = "Румынская тяга", muscleGroup = "HAMSTRINGS", equipment = "barbell"),
    ExerciseEntity(name = "Подъёмы на носки", muscleGroup = "CALVES", equipment = "machine"),
    
    // Abs
    ExerciseEntity(name = "Скручивания", muscleGroup = "ABS", equipment = "bodyweight"),
    ExerciseEntity(name = "Планка", muscleGroup = "ABS", equipment = "bodyweight"),
    ExerciseEntity(name = "Подъём ног в висе", muscleGroup = "ABS", equipment = "bodyweight")
)
