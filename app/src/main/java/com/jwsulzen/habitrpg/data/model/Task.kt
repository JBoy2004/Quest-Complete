package com.jwsulzen.habitrpg.data.model

data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val skillId: String,
    val difficulty: Difficulty,
    /*val schedule: Schedule,*/
    /*val notification : Notification,*/ //TODO add optional notifications per task!
    val isCustom: Boolean
)