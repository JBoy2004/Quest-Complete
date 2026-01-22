package com.jwsulzen.habitrpg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.jwsulzen.habitrpg.data.local.AppDatabase
import com.jwsulzen.habitrpg.data.repository.GameRepository
import com.jwsulzen.habitrpg.ui.navigation.Navigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initialize database and repository
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = GameRepository(db.taskDao())
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