package com.jwsulzen.habitrpg.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jwsulzen.habitrpg.HabitRpgApplication
import com.jwsulzen.habitrpg.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    //Dedicated scope for background work in the receiver
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id") ?: return
        val taskTitle = intent.getStringExtra("task_title") ?: "Quest Reminder"

        //Grab repository from Application class
        val app = context.applicationContext as HabitRpgApplication
        val repository = app.repository

        //Trigger the notification UI
        showNotification(context, taskId, taskTitle)

        //Handle async work (rescheduling)
        val pendingResult = goAsync() //don't kill for up to 10 seconds
        scope.launch {
            try {
                val task = repository.getTaskById(taskId)
                task?.let {
                    val scheduler = NotificationScheduler(context)
                    scheduler.scheduleNotification(it)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, taskId: String, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "quest_reminders"

        //Create Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Quest Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) //TODO replace with actual icon
            .setContentTitle(title)
            .setContentText("Time for your Quest!") //TODO change?
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId.hashCode(), notification)
    }
}