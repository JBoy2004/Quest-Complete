package com.jwsulzen.habitrpg.data.local

import androidx.room.*
import com.jwsulzen.habitrpg.data.model.SkillProgress
import com.jwsulzen.habitrpg.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM skill_progress")
    fun getSkillProgress(): Flow<List<SkillProgress>>

    @Query("SELECT * FROM skill_progress")
    suspend fun getSkillProgressAsList(): List<SkillProgress>

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    @Query("DELETE FROM skill_progress")
    suspend fun clearAllProgress()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSkillProgress(progress: SkillProgress)
}