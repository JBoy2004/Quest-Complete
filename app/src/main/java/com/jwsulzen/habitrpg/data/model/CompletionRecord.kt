package com.jwsulzen.habitrpg.data.model

import java.time.Instant

data class CompletionRecord(
    val taskId: String,
    val timestamp: Long = Instant.now().toEpochMilli()
)