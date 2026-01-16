package com.jwsulzen.habitrpg.data.model

import com.jwsulzen.habitrpg.data.model.SkillProgress

data class PlayerState (
    val skills: Map<String, SkillProgress>
)