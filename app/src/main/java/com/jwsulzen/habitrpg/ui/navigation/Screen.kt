package com.jwsulzen.habitrpg.ui.navigation

sealed class Screen(val route: String) {
    object TasklistScreen : Screen("task_list_screen")
    object SelectSkillScreen : Screen("select_skill_screen")
    object TaskSettingsScreen : Screen("task_settings_screen")
    object StatsScreen : Screen("stats_screen")
    object StatsDetailScreen : Screen("stats_detail_screen") {
        const val routeWithArgs = "stats_detail_screen/{id}/{type}"
    }
}