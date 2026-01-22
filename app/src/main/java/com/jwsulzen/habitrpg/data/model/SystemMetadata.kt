package com.jwsulzen.habitrpg.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "system_metadata")
data class SystemMetadata(
    @PrimaryKey val id: Int = 0,
    val lastRefreshDate: LocalDate
)