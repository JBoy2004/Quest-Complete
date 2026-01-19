package com.jwsulzen.habitrpg.data.repository

//TODO: split into TaskRepository, PlayerRepository, SkillRepository?

import android.os.Build
import com.jwsulzen.habitrpg.data.local.TaskDao
import com.jwsulzen.habitrpg.data.model.*
import com.jwsulzen.habitrpg.data.seed.DefaultSkills
import com.jwsulzen.habitrpg.data.seed.DefaultTasks
import com.jwsulzen.habitrpg.domain.RpgEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

class GameRepository(private val taskDao: TaskDao) {
    //STORE IN MEMORY (Static Data)
    private val _tasksSuggestedList = MutableStateFlow(DefaultTasks.tasks)
    val tasksSuggestedList = _tasksSuggestedList.asStateFlow()
    private val _skills = MutableStateFlow(DefaultSkills.skills)
    val skills = _skills.asStateFlow()

    //STORE IN DATABASE (Dynamic Data)
    val tasksCurrentList: Flow<List<Task>> = taskDao.getAllTasks()

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

        //3. Remove from db
        taskDao.deleteTask(task)
    }

    suspend fun createTask(title: String, description: String, skillId: String, difficulty: Difficulty/*,schedule: Schedule*/) {
        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            skillId = skillId,
            difficulty = difficulty,
            /*schedule = schedule,*/
            isCustom = true
        )
        taskDao.insertTask(newTask)
    }

    suspend fun resetGameData() {
        taskDao.clearAllTasks()
        taskDao.clearAllProgress()
        _completionHistory.value = emptyList<CompletionRecord>()
    }

    //TODO implement scheduling and this
    /*
    fun refreshDailyTasks() {
        val masterList = _tasksSuggestedList.value
        val dueToday = masterList.filter { task ->
            true
        }

        _tasksCurrentList.value = dueToday
    }
    */
}