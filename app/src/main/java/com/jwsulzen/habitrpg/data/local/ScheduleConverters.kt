package com.jwsulzen.habitrpg.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.jwsulzen.habitrpg.data.model.Schedule
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScheduleConverters {
    private val gson = Gson()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromSchedule(schedule: Schedule): String {
        val json = JsonObject()
        when (schedule) {
            is Schedule.Daily -> json.addProperty("type", "DAILY")
            is Schedule.Weekly -> {
                json.addProperty("type", "WEEKLY")
                json.add("days", gson.toJsonTree(schedule.days))
            }
            is Schedule.Monthly -> {
                json.addProperty("type", "MONTHLY")
                json.addProperty("day", schedule.dayOfMonth)
            }
            is Schedule.Interval -> {
                json.addProperty("type", "INTERVAL")
                json.addProperty("interval", schedule.everyXDays)
                json.addProperty("start", schedule.startDate.format(formatter))
            }
        }
        return json.toString()
    }

    @TypeConverter
    fun toSchedule(json: String): Schedule {
        val obj = gson.fromJson(json, JsonObject::class.java)
        return when (val type = obj.get("type").asString) {
            "DAILY" -> Schedule.Daily
            "WEEKLY" -> {
                val daysType = object : com.google.gson.reflect.TypeToken<Set<java.time.DayOfWeek>>() {}.type
                Schedule.Weekly(gson.fromJson(obj.get("days"), daysType))
            }
            "MONTHLY" -> Schedule.Monthly(obj.get("day").asInt)
            "INTERVAL" -> Schedule.Interval(
                obj.get("interval").asInt,
                LocalDate.parse(obj.get("start").asString, formatter)
            )
            else -> Schedule.Daily
        }
    }
}