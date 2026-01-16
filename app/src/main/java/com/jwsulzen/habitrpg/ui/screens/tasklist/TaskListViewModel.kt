package com.jwsulzen.habitrpg.ui.screens.tasklist

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jwsulzen.habitrpg.data.model.Task
import com.jwsulzen.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskListViewModel : ViewModel() {

    //"collect" tasks from the repository
    //UI will "observe" this list
    val level = GameRepository.globalLevel //Flow<Int>
    val totalXp = GameRepository.totalXp // Flow<Int>
    val tasks: StateFlow<List<Task>> = GameRepository.tasksCurrentList

    //Called when user clicks checkbox
    fun onTaskCompleted(task: Task) {
        viewModelScope.launch { //coroutine!
            GameRepository.completeTask(task)
        }
    }
}