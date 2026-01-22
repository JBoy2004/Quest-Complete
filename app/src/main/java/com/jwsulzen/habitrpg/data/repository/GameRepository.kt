package com.jwsulzen.habitrpg.data.repository

//TODO: split into TaskRepository, PlayerRepository, SkillRepository?

import android.os.Build
import com.jwsulzen.habitrpg.data.local.TaskDao
import com.jwsulzen.habitrpg.data.model.*
import com.jwsulzen.habitrpg.data.seed.DefaultSkills
import com.jwsulzen.habitrpg.data.seed.DefaultTasks
import com.jwsulzen.habitrpg.data.model.SystemMetadata
import com.jwsulzen.habitrpg.domain.RpgEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import kotlin.Boolean

class GameRepository(private val taskDao: TaskDao) {
    //------------------- DATA STREAMS -------------------
    //STATIC: Suggested blueprints
    private val _tasksSuggestedList = MutableStateFlow(DefaultTasks.tasks)
    val tasksSuggestedList = _tasksSuggestedList.asStateFlow()
    //STATIC: Skill definitions
    private val _skills = MutableStateFlow(DefaultSkills.skills)
    val skills = _skills.asStateFlow()

    //DYNAMIC: "Active Quest Log" from DB
    val tasksCurrentList: Flow<List<Task>> = taskDao.getActiveTasks()
    //DYNAMIC: Player Progress mapped from DB
    val playerState: Flow<PlayerState> = taskDao.getSkillProgress().map { list ->
        //convert list from database into map
        val skillMap = list.associateBy { it.skillId }

        //If db is empty initialize from hardcoded defaults (0 xp, lvl 1)
        if (skillMap.isEmpty()) {
            PlayerState(skills = DefaultSkills.skills.associate { it.id to SkillProgress(it.id, 0, 1) })
        } else {
            PlayerState(skills = skillMap)
        }
    }

    //Level State
    val totalXp: Flow<Int> = playerState.map { state ->
        state.skills.values.sumOf { it.xp }
    }

    val globalLevel: Flow<Int> = totalXp.map { xp ->
        RpgEngine.getLevelFromTotalXp(xp)
    }

    //History (List of completed task Ids and timestamps)
    private val _completionHistory = MutableStateFlow<List<CompletionRecord>>(emptyList())
    val completionHistory = _completionHistory.asStateFlow()

    // ---------------------- ACTIONS ----------------------

    suspend fun completeTask(task: Task) {
        //1. Add to history
        val record = CompletionRecord(taskId = task.id)
        _completionHistory.update {it + record}

        //2. Update Skill Progress
        val progressList = taskDao.getSkillProgressAsList()
        val currentProgress = progressList.find { it.skillId == task.skillId }
            ?: SkillProgress(task.skillId, 0, 1) //if it doesn't exist, start at lvl 1, 0 xp

        val newXp = currentProgress.xp + task.difficulty.baseXp
        val newLevel = RpgEngine.getLevelFromTotalXp(newXp)

        val updatedProgress = currentProgress.copy(
            xp = newXp,
            level = newLevel
        )

        taskDao.updateSkillProgress(updatedProgress)

        //3. Set current task to inactive
        taskDao.updateTaskActiveStatus(task.id, false)
    }

    suspend fun createTask(title: String, description: String, skillId: String, difficulty: Difficulty,schedule: Schedule) {
        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            skillId = skillId,
            difficulty = difficulty,
            schedule = schedule,
            isActive = true
        )
        taskDao.insertTask(newTask)
    }

    suspend fun resetGameData() {
        taskDao.clearAllTasks()
        taskDao.clearAllProgress()
        _completionHistory.value = emptyList()
    }

    suspend fun refreshDailyTasks() {
        val today = LocalDate.now()

        //1. Check DB for the last refresh date
        val metadata = taskDao.getMetadata()
        val lastRefreshDate = metadata?.lastRefreshDate

        //2. If already refreshed today, stop
        if (lastRefreshDate == today) return

        //3. Perform Daily Reset logic
        taskDao.deactivateAllTasks()

        val allTasks = taskDao.getAllTasksAsList()
        allTasks.forEach { task ->
            if (task.schedule.isDue(today)) {
                taskDao.updateTaskActiveStatus(task.id, true)
            }
        }

        //4. Update DB with today's date for refreshing
        taskDao.updateMetadata(SystemMetadata(lastRefreshDate = today))
    }
}