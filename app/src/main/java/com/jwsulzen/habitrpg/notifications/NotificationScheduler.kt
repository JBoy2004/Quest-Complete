package com.jwsulzen.habitrpg.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.os.unregisterForAllProfilingResults
import com.jwsulzen.habitrpg.data.model.Task
import java.time.DayOfWeek
import java.util.*

class NotificationScheduler(private val context: Context) {

    //HELPER: Remap java.time.DayOfWeek to java.util.Calendar
    private fun DayOfWeek.toCalendarDay(): Int {
        return when (this) {
            DayOfWeek.SUNDAY -> Calendar.SUNDAY
            DayOfWeek.MONDAY -> Calendar.MONDAY
            DayOfWeek.TUESDAY -> Calendar.TUESDAY
            DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
            DayOfWeek.THURSDAY -> Calendar.THURSDAY
            DayOfWeek.FRIDAY -> Calendar.FRIDAY
            DayOfWeek.SATURDAY -> Calendar.SATURDAY
        }
    }

    fun scheduleNotification(task: Task) {
        val settings = task.notificationSettings ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.hour)
            set(Calendar.MINUTE, settings.minute)
            set(Calendar.SECOND, 0)
        }

        //Use mapping here
        val activeCalendarDays = settings.daysOfWeek.map { it.toCalendarDay() }

        //Start checking from today
        var daysToAdd = 0
        val tempCalendar = calendar.clone() as Calendar

        //Loop up to 7 days to find the next active day
        while (!activeCalendarDays.contains(tempCalendar.get(Calendar.DAY_OF_WEEK)) ||
                tempCalendar.before(Calendar.getInstance())) {
            tempCalendar.add(Calendar.DATE, 1)
            daysToAdd++

            //Safety break to prevent infinite loop if no days are selected
            if (daysToAdd > 7) break
        }

        //Apply the calculated day to the main calendar
        calendar.add(Calendar.DATE, daysToAdd)

        //Schedule the exact alarm
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            //Handle cases where SCHEDULE_EXACT_ALARM permission is denied on Android 14+
        }
    }

    fun cancelNotification(taskId: String) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
}