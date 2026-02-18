package com.jwsulzen.habitrpg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.jwsulzen.habitrpg.ui.navigation.Navigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Grab repository from Application class
        val app = application as HabitRpgApplication
        val repository = app.repository

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                repository.refreshDailyTasks()
            }
        }

        enableEdgeToEdge()
        setContent {
            Navigation(repository = repository)
        }
    }
}