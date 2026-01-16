package com.jwsulzen.habitrpg.ui.screens.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jwsulzen.habitrpg.data.model.Difficulty
import com.jwsulzen.habitrpg.data.model.Task
import com.jwsulzen.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddTaskViewModel : ViewModel() {

    //"collect" tasks from the repository
    //UI will "observe" this list
    val tasks: StateFlow<List<Task>> = GameRepository.tasksCurrentList

    //TODO add functions to add custom tasks and premade tasks to CurrentTaskList
    fun onAddTask(
        title : String,
        description : String,
        skillId : String,
        difficulty : Difficulty/*
        schedule: Schedule*/
    ) {
        viewModelScope.launch { //coroutine
            GameRepository.createTask(
                title,
                description,
                skillId,
                difficulty/*,
                schedule,*/
            )
        }
    }
}