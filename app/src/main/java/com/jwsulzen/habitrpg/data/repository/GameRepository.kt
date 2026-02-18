package com.jwsulzen.habitrpg.data.repository

//TODO: split into TaskRepository, PlayerRepository, SkillRepository?

import com.jwsulzen.habitrpg.data.local.TaskDao
import com.jwsulzen.habitrpg.data.model.*
import com.jwsulzen.habitrpg.data.seed.DefaultSkills
import com.jwsulzen.habitrpg.data.seed.TaskTemplates
import com.jwsulzen.habitrpg.data.model.SystemMetadata
import com.jwsulzen.habitrpg.domain.RpgEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

class GameRepository(private val taskDao: TaskDao) {
    //------------------- DATA STREAMS -------------------
    //STATIC: Suggested blueprints
    private val _tasksSuggestedList = MutableStateFlow(TaskTemplates.templates)
    val tasksSuggestedList = _tasksSuggestedList.asStateFlow()
    //STATIC: Skill definitions
    private val _skills = MutableStateFlow(DefaultSkills.skills)
    val skills = _skills.asStateFlow()

    //DYNAMIC: Pull all tasks
    val tasksCurrentList: Flow<List<Task>> = taskDao.getAllTasks()
    val tasksDaily: Flow<List<Task>> = taskDao.getDailyTasks()
    val tasksWeekly: Flow<List<Task>> = taskDao.getWeeklyTasks()
    val tasksMonthly: Flow<List<Task>> = taskDao.getMonthlyTasks()
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

    suspend fun createTask(title: String, skillId: String, difficulty: Difficulty,schedule: Schedule, goal: Int, unit: String, isMeasurable: Boolean, notificationSettings : NotificationSettings? = null): Task {
        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            skillId = skillId,
            difficulty = difficulty,
            schedule = schedule,
            goal = goal,
            currentProgress = 0,
            isGoalReached = false,
            unit = unit,
            isMeasurable = isMeasurable,
            notificationSettings = notificationSettings
        )
        taskDao.insertTask(newTask)
        return newTask
    }

    private fun getPeriodStart(schedule: Schedule, date: LocalDate): LocalDate {
        return when (schedule) {
            is Schedule.Daily -> date
            is Schedule.Weekly -> date.with(java.time.DayOfWeek.MONDAY) //TODO User setting for Sun/Mon
            is Schedule.Monthly -> date.withDayOfMonth(1)
            is Schedule.Interval -> date //TODO (low priority) add custom logic later
        }
    }

    suspend fun logTaskProgress(task: Task, amount: Int, date: LocalDate, newGoal: Int) {
        val today = LocalDate.now()

        //Get existing record to see how much XP was saved
        val existingRecord = taskDao.getCompletionRecord(task.id, date)
        val oldXpGained = existingRecord?.xpGained ?: 0

        //Calculate XP pools
        val progressXpPool = task.difficulty.baseXp
        val completionBonusPool = when (task.schedule) {
            is Schedule.Daily -> (task.difficulty.baseXp / 2f).toInt()
            is Schedule.Weekly -> task.difficulty.baseXp
            is Schedule.Monthly -> (task.difficulty.baseXp * 1.25f).toInt()
            else -> (task.difficulty.baseXp / 2f).toInt()
        }

        //Calculate how much Progress XP to award/subtract
        val progressRatio = (amount.toFloat() / newGoal.toFloat())
        val currentProgressXp = (progressRatio * progressXpPool).toInt()

        //Determine if goal is reached ON THAT DATE
        val periodStart = getPeriodStart(task.schedule, date)
        val otherDaysProgress = (taskDao.getProgressSumForRange(task.id, periodStart, today) ?: 0) - (existingRecord?.progressAmount ?: 0)
        val totalProgressWithNewAmount = otherDaysProgress + amount

        val isGoalMet = totalProgressWithNewAmount >= newGoal
        val finalRecordXp = if (isGoalMet) currentProgressXp + completionBonusPool else currentProgressXp

        //Calculate Delta and update Skill
        val xpDelta = finalRecordXp - oldXpGained
        updateSkillXp(task.skillId, xpDelta)

        //Save the record with updated xpGained
        val record = CompletionRecord(
            taskId = task.id,
            date = date,
            xpGained = finalRecordXp,
            progressAmount = amount
        )
        taskDao.insertCompletionRecord(record) //overwrite the previous data

        //Update Goal in DB if it changed
        val currentPeriodStart = getPeriodStart(task.schedule, today)
        val isDateInCurrentPeriod = !date.isBefore(currentPeriodStart) && !date.isAfter(today)
        if (isDateInCurrentPeriod) {
            val currentTotal = taskDao.getProgressSumForRange(task.id, currentPeriodStart, today) ?: 0

            val updatedTask = task.copy(
                goal = newGoal,
                currentProgress = currentTotal,
                isGoalReached = currentTotal >= newGoal
            )
            taskDao.insertTask(updatedTask)
        } else if (newGoal != task.goal) {
            //If edited historical date but also changed the goal
            taskDao.updateTaskGoal(task.id, newGoal)
        }
    }

    private suspend fun updateSkillXp(skillId: String, xpGain: Int) {
        val currentSkillProgress = taskDao.getSkillProgressAsList()
            .find { it.skillId == skillId }
            ?: SkillProgress(skillId, 0, 1)

        val newXp = currentSkillProgress.xp + xpGain
        val newLevel = RpgEngine.getLevelFromTotalXp(newXp)

        taskDao.updateSkillProgress(currentSkillProgress.copy(xp = newXp, level = newLevel))
    }

    suspend fun completeTask(task: Task, amount: Int) {
        val currentAmountToday = taskDao.getProgressForTaskOnDate(task.id, LocalDate.now()) ?: 0
        //Prevent negative logs
        val newAmount = maxOf(0, currentAmountToday + amount)
        logTaskProgress(
            task,
            newAmount,
            LocalDate.now(),
            task.goal
        )
    }

    suspend fun getProgressForTaskOnDate(taskId: String, date: LocalDate): Int {
        return taskDao.getProgressForTaskOnDate(taskId, date) ?: 0
    }

    suspend fun getDatesWithProgress(taskId: String): List<LocalDate> {
        return taskDao.getDatesWithProgress(taskId)
    }

    suspend fun updateTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun getTaskById(id: String): Task? {
        return taskDao.getTaskById(id)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    fun getHistoryForFilter(filterId: String): Flow<List<CompletionRecord>> {
        return taskDao.getHistoryForFilter(filterId)
    }

    suspend fun getActivityDates(skillId: String, daysBack: Long): List <LocalDate> {
        val startDate = LocalDate.now().minusDays(daysBack)
        return taskDao.getActivityDates(skillId, startDate)
    }

    suspend fun resetGameData() {
        taskDao.clearAllTasks()
        taskDao.clearAllProgress()
        _completionHistory.value = emptyList()
    }

    suspend fun refreshDailyTasks() {
        val today = LocalDate.now()
        val metadata = taskDao.getMetadata()
        val lastRefreshDate = metadata?.lastRefreshDate ?: LocalDate.MIN

        //If already refreshed today, stop
        if (lastRefreshDate == today) return

        //Get all tasks to check their individual schedules
        val allTasks = taskDao.getAllTasksAsList()

        allTasks.forEach { task ->
            val shouldReset = when (val x = task.schedule) {
                is Schedule.Daily -> true //Always reset daily tasks
                is Schedule.Weekly -> {
                    //Reset if today is a new week
                    //TODO allow user to set start of week to Sunday or Monday
                    val lastWeek = lastRefreshDate.with(java.time.DayOfWeek.MONDAY)
                    val currentWeek = today.with(java.time.DayOfWeek.MONDAY)
                    currentWeek != lastWeek
                }
                is Schedule.Monthly -> {
                    //Reset if today is a new month
                    today.monthValue != lastRefreshDate.monthValue || today.year != lastRefreshDate.year
                }
                is Schedule.Interval -> {
                    //Currently handling like "session" reset TODO Make robust interval logic or scrap it
                    task.isGoalReached
                }
            }

            if (shouldReset) {
                taskDao.insertTask(task.copy(currentProgress = 0, isGoalReached = false))
            }
        }

        //4. Update DB with today's date
        taskDao.updateMetadata(SystemMetadata(lastRefreshDate = today))
    }
}