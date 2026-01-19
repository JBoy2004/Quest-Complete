package com.jwsulzen.habitrpg.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jwsulzen.habitrpg.data.model.Task
import com.jwsulzen.habitrpg.data.repository.GameRepository
import com.jwsulzen.habitrpg.ui.screens.tasklist.TaskListViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatsViewModel(private val repository: GameRepository) : ViewModel() {
    val playerState = repository.playerState
    val globalLevel = repository.globalLevel
    val totalXp = repository.totalXp
    val completionHistory = repository.completionHistory

    companion object {
        fun provideFactory(repository: GameRepository): ViewModelProvider.Factory = object :
            ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StatsViewModel(repository) as T
            }
        }
    }

    fun onResetData() {
        viewModelScope.launch {
            repository.resetGameData()
        }
    }
}