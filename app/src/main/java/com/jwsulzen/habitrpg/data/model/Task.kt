package com.jwsulzen.habitrpg.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val skillId: String,
    val difficulty: Difficulty,
    /*val schedule: Schedule,*/
    /*val notification : Notification,*/ //TODO add optional notifications per task!
    val isCustom: Boolean
)