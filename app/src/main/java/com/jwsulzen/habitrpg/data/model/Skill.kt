package com.jwsulzen.habitrpg.data.model

data class Skill (
    val id: String,
    val name: String,
    val progressionType: ProgressionType,
    val emoji: String = "❓" //? is default fallback
    //TODO add icon art instead of emojis
    )