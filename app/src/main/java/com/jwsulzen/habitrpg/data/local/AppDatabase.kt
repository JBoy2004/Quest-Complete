package com.jwsulzen.habitrpg.data.local

import android.app.Notification
import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.jwsulzen.habitrpg.data.model.CompletionRecord
import com.jwsulzen.habitrpg.data.model.Difficulty
import com.jwsulzen.habitrpg.data.model.SkillProgress
import com.jwsulzen.habitrpg.data.model.SystemMetadata
import com.jwsulzen.habitrpg.data.model.NotificationSettings
import com.jwsulzen.habitrpg.data.model.Task
import java.time.LocalDate

@Database(entities = [Task::class, SkillProgress::class, SystemMetadata::class, CompletionRecord::class],
    version = 11, //Increment when adding/modifying classes in DB
    exportSchema = false)
@TypeConverters(AppConverters::class, ScheduleConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE tasks ADD COLUMN notificationSettings TEXT"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_rpg_database"
                ).addMigrations(MIGRATION_10_11)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

//Helpers to store as strings/jsons
class AppConverters {
    @TypeConverter
    fun fromDifficulty(value: Difficulty) = value.name
    @TypeConverter
    fun toDifficulty(value: String) = Difficulty.valueOf(value)

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? = dateString?.let { LocalDate.parse(it) }

    private val gson = Gson()

    @TypeConverter
    fun fromNotificationSettings(value: NotificationSettings?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toNotificationSettings(value: String?): NotificationSettings? {
        return value?.let { gson.fromJson(it, NotificationSettings::class.java) }
    }
}