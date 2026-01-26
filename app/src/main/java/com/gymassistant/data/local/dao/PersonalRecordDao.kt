package com.gymassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gymassistant.data.local.entity.PersonalRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {

    @Query("SELECT * FROM personal_records ORDER BY achievedAt DESC")
    fun getAllRecords(): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId")
    suspend fun getRecordForExercise(exerciseId: Long): PersonalRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecord(record: PersonalRecordEntity)

    @Query("""
        SELECT pr.* FROM personal_records pr
        INNER JOIN exercises e ON pr.exerciseId = e.id
        ORDER BY pr.achievedAt DESC
        LIMIT :limit
    """)
    fun getRecentRecords(limit: Int = 10): Flow<List<PersonalRecordEntity>>
}
