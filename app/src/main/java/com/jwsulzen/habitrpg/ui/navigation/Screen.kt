package com.jwsulzen.habitrpg.ui.navigation

sealed class Screen(val route: String) {
    object TasklistScreen : Screen("task_list_screen")
    object AddTaskScreen : Screen("add_task_screen")
    object StatsScreen : Screen("stats_screen")

    /* For building dynamic routes (currently unused)
    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
    */
}