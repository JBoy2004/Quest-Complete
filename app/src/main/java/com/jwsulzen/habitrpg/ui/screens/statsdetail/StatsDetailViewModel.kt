package com.jwsulzen.habitrpg.ui.screens.statsdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jwsulzen.habitrpg.data.model.CompletionRecord
import com.jwsulzen.habitrpg.data.repository.GameRepository
import com.jwsulzen.habitrpg.data.seed.DefaultSkills
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatsDetailViewModel(
    private val repository: GameRepository,
    private val id: String,
    private val type: String
) : ViewModel() {

    var selectedSkillGroup by mutableStateOf("Overall")
    var finalFilterTarget by mutableStateOf("Overall")

    init {
        viewModelScope.launch {
            when (type) {
                "overall" -> {
                    selectedSkillGroup = "Overall"
                    finalFilterTarget = "Overall"
                }
                "skill" -> {
                    selectedSkillGroup = id
                    finalFilterTarget = id
                }
                "task" -> {
                    val task = repository.getTaskById(id)
                    selectedSkillGroup = task?.skillId ?: "Overall"
                    finalFilterTarget = id
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredHistory: StateFlow<List<CompletionRecord>> = snapshotFlow { finalFilterTarget }
        .flatMapLatest { target ->
            repository.getHistoryForFilter(target)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playerState = repository.playerState
    val globalLevel = repository.globalLevel
    val totalXp = repository.totalXp
    val completionHistory = repository.completionHistory

    companion object {
        fun provideFactory(
            repository: GameRepository,
            id: String,
            type: String
        ): ViewModelProvider.Factory = object :
            ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StatsDetailViewModel(repository, id, type) as T
            }
        }
    }

    val allTasks = repository.tasksCurrentList
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
}