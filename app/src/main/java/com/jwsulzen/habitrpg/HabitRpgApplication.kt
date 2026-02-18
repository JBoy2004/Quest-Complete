package com.jwsulzen.habitrpg

import android.app.Application
import com.jwsulzen.habitrpg.data.local.AppDatabase
import com.jwsulzen.habitrpg.data.repository.GameRepository

class HabitRpgApplication : Application() {
    //lazy = created when first needed
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { GameRepository(database.taskDao()) }
}