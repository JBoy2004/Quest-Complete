package com.jwsulzen.habitrpg.data.model

import java.time.DayOfWeek
import java.time.LocalDate

sealed class Schedule {
    abstract fun isDue(date: LocalDate): Boolean

    data object Daily : Schedule() {
        override fun isDue(date: LocalDate) = true
    }

    data class Weekly(val days: Set<DayOfWeek>) : Schedule() {
        override fun isDue(date: LocalDate) = days.contains(date.dayOfWeek)
    }

    data class Monthly(val dayOfMonth: Int) : Schedule() {
        override fun isDue(date: LocalDate) = date.dayOfMonth == dayOfMonth
    }

    data class Interval(val everyXDays: Int, val startDate: LocalDate) : Schedule() {
        override fun isDue(date: LocalDate): Boolean {
            val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, date)
            return daysBetween >= 0 && daysBetween % everyXDays == 0L
        }
    }
}