package com.jwsulzen.habitrpg.ui.screens.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jwsulzen.habitrpg.data.model.Difficulty
import com.jwsulzen.habitrpg.data.model.Schedule
import com.jwsulzen.habitrpg.data.model.Task
import com.jwsulzen.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddTaskViewModel(private val repository: GameRepository) : ViewModel() {

    val tasks = repository.tasksCurrentList.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    //TODO add functions to add custom tasks and premade tasks to CurrentTaskList
    fun onAddTask(
        title : String,
        description : String,
        skillId : String,
        difficulty : Difficulty,
        schedule: Schedule
    ) {
        viewModelScope.launch { //coroutine
            repository.createTask(
                title,
                description,
                skillId,
                difficulty,
                schedule
            )
        }
    }

    companion object {
        fun provideFactory(repository: GameRepository): ViewModelProvider.Factory = object :
            ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddTaskViewModel(repository) as T
            }
        }
    }
}