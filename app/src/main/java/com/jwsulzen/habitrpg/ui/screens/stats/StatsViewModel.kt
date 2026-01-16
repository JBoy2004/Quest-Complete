package com.jwsulzen.habitrpg.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jwsulzen.habitrpg.data.model.Task
import com.jwsulzen.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatsViewModel : ViewModel() {
    val playerState = GameRepository.playerState

    val globalLevel = GameRepository.globalLevel
    val totalXp = GameRepository.totalXp

    val completionHistory = GameRepository.completionHistory
}