package com.jwsulzen.habitrpg.data.repository

//TODO: split into TaskRepository, PlayerRepository, SkillRepository

import android.os.Build
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

object GameRepository {
    //Tasks State
    private val _tasksSuggestedList = MutableStateFlow(DefaultTasks.tasks)
    val tasksSuggestedList = _tasksSuggestedList.asStateFlow()

    private val _tasksCurrentList = MutableStateFlow(DefaultTasks.tasks) //TODO different default tasks customized from introduction
    val tasksCurrentList = _tasksCurrentList.asStateFlow()

    //Skills State
    private val _skills = MutableStateFlow(DefaultSkills.skills)
    val skills = _skills.asStateFlow()

    //Player Progress State (Map)
    private val initialProgress = DefaultSkills.skills.associate { skill ->
        skill.id to SkillProgress(
            skillId = skill.id,
            xp = 0,
            level = 1/*,
            lastCompletedAt = LocalDate.now()*/
        )
    }
    private val _playerState = MutableStateFlow(PlayerState(skills = initialProgress))
    val playerState = _playerState.asStateFlow()

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

    fun completeTask(task: Task) {
        //1. Add to history
        val record = CompletionRecord(taskId = task.id)
        _completionHistory.update {it + record}

        //2. Update Skill Progress
        _playerState.update { currentState ->
            val currentProgress = currentState.skills[task.skillId] //try to find skill
                ?: SkillProgress(task.skillId, 0, 1/*, LocalDate.now()*/) //if skill not found, set progress to 0 for it(?)

            val newXp = currentProgress.xp + task.difficulty.baseXp //TODO could move this into RpgEngine to make more robust xp gain system (with multipliers, etc.)

            //Determine level for this specific skill based on its new xp
            val newLevel = RpgEngine.getLevelFromTotalXp(newXp)

            //Create the updated progress object
            val updatedProgress = currentProgress.copy(
                xp = newXp,
                level = newLevel
                /*lastCompletedAt = LocalDate.now()*/
            )

            //Return new PlayerState with the updated Map
            currentState.copy(
                skills = currentState.skills + (task.skillId to updatedProgress)
            )
        }

        //3. Remove from the current active list
        _tasksCurrentList.update { list ->
            list.filterNot { it.id == task.id }
        }
    }

    fun createTask(
        title: String,
        description: String,
        skillId: String,
        difficulty: Difficulty/*,
        schedule: Schedule*/
    ) {
        val newTask = Task(
            id = UUID.randomUUID().toString(), //random string for custom tasks, is this necessary? users will only save tasks locally
            title = title,
            description = description,
            skillId = skillId,
            difficulty = difficulty,
            /*schedule = schedule,*/
            isCustom = true
        )
        _tasksCurrentList.update { it + newTask }
    }

    //TODO implement scheduling and this
    fun refreshDailyTasks() {
        val masterList = _tasksSuggestedList.value
        val dueToday = masterList.filter { task ->
            //TODO add scheduling logic
            true
        }

        _tasksCurrentList.value = dueToday
    }
}