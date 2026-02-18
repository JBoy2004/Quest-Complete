package com.jwsulzen.habitrpg.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jwsulzen.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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

    val allTasks = repository.tasksCurrentList
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun onResetData() {
        viewModelScope.launch {
            repository.resetGameData()
        }
    }
}