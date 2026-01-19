package com.jwsulzen.habitrpg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jwsulzen.habitrpg.data.local.AppDatabase
import com.jwsulzen.habitrpg.data.repository.GameRepository
import com.jwsulzen.habitrpg.ui.navigation.Navigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initialize database and repository
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = GameRepository(db.taskDao())
        enableEdgeToEdge()
        setContent {
            Navigation(repository = repository)
        }
    }
}