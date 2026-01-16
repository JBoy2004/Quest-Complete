package com.jwsulzen.habitrpg.data.model

import java.time.LocalDate

data class SkillProgress (
    val skillId: String,
    val xp: Int,
    val level: Int/*,
    val lastCompletedAt: LocalDate*/ //For decaying tasks? WIP, might use a different system
)