package com.jwsulzen.habitrpg.ui.screens.tasksettings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jwsulzen.habitrpg.data.model.Difficulty
import com.jwsulzen.habitrpg.data.model.Schedule
import com.jwsulzen.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskSettingsViewModel(
    private val repository: GameRepository,
    private val taskId: String?
) : ViewModel() {

    //Notification States
    var hasNotification by mutableStateOf(false)
    var notificationHour by mutableStateOf(8)
    var notificationMinute by mutableStateOf(0)
    var notificationDays by mutableStateOf(java.time.DayOfWeek.entries.toSet())

    //Helper to show days in the UI (e.g. Mon, Tue, Wed)
    val notificationDaysShort: String
        get() = if (notificationDays.size == 7) "Every day"
        else notificationDays.joinToString(", ") { it.name.take(3).lowercase().replaceFirstChar { it.uppercase() } }

    fun toggleNotificationDay(day: java.time.DayOfWeek) {
        notificationDays = if (notificationDays.contains(day)) {
            notificationDays - day
        } else {
            notificationDays + day
        }
    }

    var title by mutableStateOf("")
    var goal by mutableStateOf("")
    var unit by mutableStateOf("")
    var selectedDifficulty by mutableStateOf(Difficulty.MEDIUM)
    var repeatType by mutableStateOf("Every day") //TODO make sure this is true
    var intervalValue by mutableStateOf("1")
    var selectedDate by mutableStateOf(LocalDate.now())

    init {
        //If taskId is provided, fetch and autofill
        if (!taskId.isNullOrBlank()) {
            viewModelScope.launch {
                repository.getTaskById(taskId)?.let { task ->
                    title = task.title
                    goal = task.goal.toString()
                    unit = task.unit ?: ""
                    selectedDifficulty = task.difficulty
                    selectedDate = LocalDate.now() //TODO or stored date? REMOVE THIS??
                    repeatType = when(task.schedule) {
                        is Schedule.Daily -> "Every day"
                        is Schedule.Weekly -> "Every week"
                        is Schedule.Monthly -> "Every month"
                        is Schedule.Interval -> "Custom"
                    }
                    task.notificationSettings?.let { settings ->
                        hasNotification = true
                        notificationHour = settings.hour
                        notificationMinute = settings.minute
                        notificationDays = settings.daysOfWeek
                    }
                }
            }
        }
    }

    fun onDeleteTask(context: android.content.Context, onDeleted: () -> Unit) {
        taskId?.let { id ->
            viewModelScope.launch {
                repository.getTaskById(id)?.let { task ->
                    //Cancel any pending alarms
                    val scheduler = com.jwsulzen.habitrpg.notifications.NotificationScheduler(context)
                    scheduler.cancelNotification(task.id)

                    repository.deleteTask(task)
                    onDeleted()
                }
            }
        }
    }

    fun onSaveTask(
        skillId: String,
        schedule: Schedule,
        isMeasurable: Boolean,
        context: android.content.Context
    ) {
        viewModelScope.launch {
            //Prepare notification data
            val notificationSettings = if (hasNotification) {
                com.jwsulzen.habitrpg.data.model.NotificationSettings(
                    hour = notificationHour,
                    minute = notificationMinute,
                    daysOfWeek = notificationDays
                )
            } else null

            //Save/Update task in DB
            val finalTask = if (taskId.isNullOrBlank()) {
                repository.createTask(
                    title,
                    skillId,
                    selectedDifficulty,
                    schedule,
                    goal.toIntOrNull() ?: 1,
                    unit,
                    isMeasurable,
                    notificationSettings
                )
            } else {
                //Fetch current task to preserve ID and other fields, then update
                val existingTask = repository.getTaskById(taskId)
                if (existingTask != null) {
                    val updatedTask = existingTask.copy(
                        title = title,
                        difficulty = selectedDifficulty,
                        schedule = schedule,
                        goal = goal.toIntOrNull() ?: 1,
                        unit = unit,
                        isMeasurable = isMeasurable,
                        notificationSettings = notificationSettings
                    )
                    repository.updateTask(updatedTask)
                    updatedTask
                } else {
                    null
                }
            }

            //Handle Scheduling
            val scheduler = com.jwsulzen.habitrpg.notifications.NotificationScheduler(context)
            finalTask?.let { task ->
                if (task.notificationSettings != null) {
                    scheduler.scheduleNotification(task)
                } else {
                    scheduler.cancelNotification(task.id)
                }
            }
        }
    }

    companion object {
        fun provideFactory(repository: GameRepository, taskId: String?): ViewModelProvider.Factory = object :
            ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TaskSettingsViewModel(repository, taskId) as T
            }
        }
    }
}