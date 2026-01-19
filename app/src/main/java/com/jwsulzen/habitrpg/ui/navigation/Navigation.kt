package com.jwsulzen.habitrpg.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jwsulzen.habitrpg.data.repository.GameRepository
import com.jwsulzen.habitrpg.ui.screens.addtask.AddTaskScreen
import com.jwsulzen.habitrpg.ui.screens.tasklist.TaskListScreen
import com.jwsulzen.habitrpg.ui.screens.stats.StatsScreen


@Composable
fun Navigation(repository: GameRepository) {
    val navController = rememberNavController()

    // This helps us track which screen is currently active
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    //TODO add swiping animations for each element
    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.AddTaskScreen.route) { //hide bar for add task screen
                NavigationBar {
                    // Home Button
                    NavigationBarItem(
                        selected = currentRoute == Screen.TasklistScreen.route,
                        onClick = {
                            navController.navigate(Screen.TasklistScreen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text("Home") },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) }
                    )

                    // Add Button (Central)
                    NavigationBarItem(
                        selected = false, //override default pill appearance
                        onClick = {
                            navController.navigate(Screen.AddTaskScreen.route)
                        },
                        icon = {
                            //surface/card to make diamond shape
                            androidx.compose.material3.Surface(
                                modifier = androidx.compose.ui.Modifier.size(82.dp),
                                shape = androidx.compose.foundation.shape.CutCornerShape(50), //50% cut = diamond
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    Icon(
                                        modifier = Modifier.size(48.dp),
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add"
                                    )
                                }
                            }
                        },
                        alwaysShowLabel = false //keep focus on diamond(?)
                    )

                    // Stats Button
                    NavigationBarItem(
                        selected = currentRoute == Screen.StatsScreen.route, // Update this when you have a stats route
                        onClick = {
                            navController.navigate(Screen.StatsScreen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text("Stats") },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.TasklistScreen.route
            ) {
                //TASKLIST SCREEN
                composable(
                    route = Screen.TasklistScreen.route,
                    enterTransition =  { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right) },
                    exitTransition =  { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left) }
                    ) {
                    TaskListScreen(navController = navController, repository = repository) //pass repository to the screen for viewmodel
                }
                //STATS SCREEN
                composable(
                    route = Screen.StatsScreen.route,
                    enterTransition =  { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left) },
                    exitTransition =  { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right) }
                ) {
                    StatsScreen(navController = navController, repository = repository)
                }
                //ADD TASK SCREEN (slide up/down)
                composable(
                    route = Screen.AddTaskScreen.route,
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Up//,
                             //animationSpec = tween(500) //500ms delay, try adjusting?
                        )
                    },
                    exitTransition = { //is this needed? other transition for create task?
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Down,
                            //animationSpec = tween(500) //500ms delay, try adjusting?
                        )
                    }
                    ) {
                    AddTaskScreen(navController = navController, repository = repository)
                }
            }
        }
    }
}