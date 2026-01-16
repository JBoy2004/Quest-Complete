package com.jwsulzen.habitrpg.ui.screens.tasklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jwsulzen.habitrpg.data.model.Task
import com.jwsulzen.habitrpg.data.seed.DefaultSkills
import com.jwsulzen.habitrpg.domain.RpgEngine
import com.jwsulzen.habitrpg.ui.navigation.Screen

@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskListViewModel = viewModel() //"injects" the viewmodel
) {
    //"collect" flow of tasks and make it a Compose State
    val tasks by viewModel.tasks.collectAsState()

    val currentLevel by viewModel.level.collectAsState(1)
    val currentTotalXp by viewModel.totalXp.collectAsState(0)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        //PLAYER INFO
        Card(
            shape = CutCornerShape(10),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //LEVEL DISPLAY
                Card(
                    shape = CutCornerShape(0),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.size(60.dp) //fixed size for level box
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "LVL", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = currentLevel.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                //NAME AND XP BAR
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "John Doe", //TODO add input for player name
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    //XP BAR
                    val progress = RpgEngine.getLevelProgress(currentTotalXp)
                    val xpIntoLevel = RpgEngine.getXpIntoLevel(currentTotalXp)
                    val xpRequiredForLevel = RpgEngine.getXpRequiredForLevel(currentLevel)

                    Column {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp),
                            strokeCap = StrokeCap.Butt, //sharp edges(?)
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "$xpIntoLevel / $xpRequiredForLevel XP",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        //QUEST INFO
        Text(
            text = "Current Quests",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onComplete = { viewModel.onTaskCompleted(task) } //lambda function to simplify TaskItem
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onComplete: () -> Unit) {
    //TODO Ideally pass the emoji or the Skill object directly into TaskItem
    val skillEmoji = DefaultSkills.skills.find { it.id == task.skillId }?.emoji ?: "❓"

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            //Emoji box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = skillEmoji, fontSize = 30.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column (modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.titleLarge)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.difficulty.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "  +${task.difficulty.baseXp} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            FilledIconButton (
                onClick = onComplete
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null
                )
            }
        }
    }
}