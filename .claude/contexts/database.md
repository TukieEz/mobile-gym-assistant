# Database Context — Room

## Entities

Все entity должны:
- Быть `data class` с `@Entity` аннотацией
- Иметь `@PrimaryKey(autoGenerate = true)` для id
- Использовать `Long` для id, timestamps
- Иметь `@ColumnInfo` для snake_case в БД

```kotlin
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "muscle_group")
    val muscleGroup: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

## DAOs

- Suspend functions для всех операций
- Flow для реактивных запросов
- `@Transaction` для сложных операций

```kotlin
@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name")
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity): Long
    
    @Delete
    suspend fun delete(exercise: ExerciseEntity)
}
```

## Database

- Версионирование обязательно
- Миграции для каждого изменения схемы
- `exportSchema = true` для отладки

```kotlin
@Database(
    entities = [ExerciseEntity::class, WorkoutSetEntity::class],
    version = 1,
    exportSchema = true
)
abstract class GymDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSetDao(): WorkoutSetDao
}
```

## Миграции

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE exercises ADD COLUMN notes TEXT")
    }
}
```
