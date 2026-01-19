package com.jwsulzen.habitrpg.data.local

import android.content.Context
import androidx.room.*
import com.jwsulzen.habitrpg.data.model.Difficulty
import com.jwsulzen.habitrpg.data.model.SkillProgress
import com.jwsulzen.habitrpg.data.model.Task

@Database(entities = [Task::class, SkillProgress::class], version = 1, exportSchema = false)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_rpg_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

//Helper to store DIFFICULTY enum as string in database
class AppConverters {
    @TypeConverter
    fun fromDifficulty(value: Difficulty) = value.name
    @TypeConverter
    fun toDifficulty(value: String) = Difficulty.valueOf(value)
}