package com.gymassistant.di

import android.app.Application
import androidx.room.Room
import com.gymassistant.data.local.dao.ExerciseDao
import com.gymassistant.data.local.dao.PersonalRecordDao
import com.gymassistant.data.local.dao.WorkoutSetDao
import com.gymassistant.data.local.database.GymDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGymDatabase(app: Application): GymDatabase {
        return Room.databaseBuilder(
            app,
            GymDatabase::class.java,
            "gym_assistant.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideExerciseDao(db: GymDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    @Singleton
    fun provideWorkoutSetDao(db: GymDatabase): WorkoutSetDao = db.workoutSetDao()

    @Provides
    @Singleton
    fun providePersonalRecordDao(db: GymDatabase): PersonalRecordDao = db.personalRecordDao()
}
