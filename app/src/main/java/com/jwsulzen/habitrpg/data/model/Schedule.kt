package com.jwsulzen.habitrpg.data.model

import java.time.DayOfWeek

sealed class Schedule {
    object Daily : Schedule()
    data class Weekly(val days: Set<DayOfWeek>) : Schedule()
    data class Monthly(val dayOfMonth: Int) : Schedule()
    data class Custom(val cronLikeRule: String) : Schedule() //cron is a background scheduling service
}