package com.jwsulzen.habitrpg.data.local

import androidx.room.*
import com.jwsulzen.habitrpg.data.model.SkillProgress
import com.jwsulzen.habitrpg.data.model.Task
import com.jwsulzen.habitrpg.data.model.SystemMetadata
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    //lastRefreshDate
    @Query("SELECT * FROM system_metadata WHERE id = 0")
    suspend fun getMetadata(): SystemMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMetadata(metadata: SystemMetadata)

    @Query("SELECT * FROM tasks WHERE isActive = 1")
    fun getActiveTasks(): Flow<List<Task>>

    //No longer a Flow because this is used for backend scheduling, not UI
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksAsList(): List<Task>

    @Query("UPDATE tasks SET isActive = :active WHERE id = :taskId")
    suspend fun updateTaskActiveStatus(taskId: String, active: Boolean)

    //Global reset before Daily Refresh (set all tasks inactive before re-evaluating)
    @Query("UPDATE tasks SET isActive = 0")
    suspend fun deactivateAllTasks()

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