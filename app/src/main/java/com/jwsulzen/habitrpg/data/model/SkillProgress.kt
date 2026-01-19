package com.jwsulzen.habitrpg.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
@Entity(tableName = "skill_progress")
data class SkillProgress (
    @PrimaryKey val skillId: String,
    val xp: Int,
    val level: Int/*,
    val lastCompletedAt: LocalDate*/ //For decaying tasks? WIP, might use a different system
)