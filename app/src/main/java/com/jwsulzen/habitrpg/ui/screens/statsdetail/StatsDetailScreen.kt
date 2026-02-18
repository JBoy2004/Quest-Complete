package com.jwsulzen.habitrpg.ui.screens.statsdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jwsulzen.habitrpg.data.model.PlayerState
import com.jwsulzen.habitrpg.data.repository.GameRepository
import com.jwsulzen.habitrpg.data.seed.DefaultSkills
import com.jwsulzen.habitrpg.domain.RpgEngine
import com.jwsulzen.habitrpg.ui.screens.stats.StatsViewModel
import kotlinx.coroutines.selects.select
import java.time.LocalDate

@Composable
fun StatsDetailScreen(
    navController: NavController,
    repository: GameRepository,
    id: String,
    type: String
) {
    val viewModel: StatsDetailViewModel = viewModel(
        factory = StatsDetailViewModel.provideFactory(repository, id, type)
    )
    val playerState by viewModel.playerState.collectAsState(
        initial = PlayerState(emptyMap())
    )
    val allTasks by viewModel.allTasks.collectAsState()
    val playerLevel by viewModel.globalLevel.collectAsState(initial = 1)
    val currentTotalXp by viewModel.totalXp.collectAsState(0)
    val levelProgress = RpgEngine.getLevelProgress(currentTotalXp)
    val xpIntoLevel = RpgEngine.getXpIntoLevel(currentTotalXp)
    val xpRequiredForLevel = RpgEngine.getXpRequiredForLevel(playerLevel)

    val filteredTasks = allTasks.filter { it.skillId == viewModel.selectedSkillGroup }
    //Dropdown data
    val taskOptions = remember(viewModel.selectedSkillGroup, allTasks) {
        if (filteredTasks.size >= 2) {
            listOf("All") + filteredTasks.map { it.title }
        } else {
            filteredTasks.map { it.title }
        }
    }

    val selectedTask = allTasks.find { it.id == viewModel.finalFilterTarget }
    val isTaskMode = selectedTask != null

    val history by viewModel.filteredHistory.collectAsState(initial = emptyList())

    val totalXpGained = history.sumOf { it.xpGained }
    val totalAmountLogged = history.sumOf { it.progressAmount }
    val questsCompleted = history.count { it.xpGained > 0 } //TODO THIS ISN'T TRUE? INCREMENT NEW VARIABLE IN CompletionHistory FROM GAME REPO (LogTask)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF1E9D2)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //region HEADER & DROPDOWN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                //HEADER
                Text(
                    text = "Stats",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                //DROPDOWNS
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //SKILL GROUP DROPDOWN (Left)
                    Box {
                        var showSkillMenu by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { showSkillMenu = true },
                            shape = CutCornerShape(4.dp)
                        ) {
                            val skillName = if(viewModel.selectedSkillGroup == "Overall") "Overall" else DefaultSkills.skills.find { it.id == viewModel.selectedSkillGroup }?.name ?: "Skill"
                            Text(skillName)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(
                            containerColor = Color(0xFFF1E9D2),
                            expanded = showSkillMenu,
                            onDismissRequest = { showSkillMenu = false }
                        ) {
                            (listOf("Overall") + playerState.skills.keys.toList()).forEach { skillId ->
                                DropdownMenuItem(
                                    text = {
                                        val name = if (skillId == "Overall") "Overall" else DefaultSkills.skills.find { it.id == skillId }?.name ?: skillId
                                        Text(name)
                                    },
                                    onClick = {
                                        viewModel.selectedSkillGroup = skillId
                                        viewModel.finalFilterTarget = skillId
                                        showSkillMenu = false
                                    }
                                )
                            }
                        }
                    }

                    //TASK DROPDOWN (Right)
                    Box {
                        var showTaskMenu by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { showTaskMenu = true },
                            enabled = viewModel.selectedSkillGroup != "Overall" && filteredTasks.size >= 2,
                            shape = CutCornerShape(4.dp)
                        ) {
                            val task = allTasks.find { it.id == viewModel.finalFilterTarget }
                            Text(task?.title ?: "All")
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(
                            containerColor = Color(0xFFF1E9D2),
                            expanded = showTaskMenu,
                            onDismissRequest = { showTaskMenu = false }
                        ) {
                            taskOptions.forEach { taskTitle ->
                                DropdownMenuItem(
                                    text = { Text(taskTitle) },
                                    onClick = {
                                        if (taskTitle == "All") {
                                            viewModel.finalFilterTarget = viewModel.selectedSkillGroup //All tasks for that group!
                                        } else {
                                            val selectedTask = allTasks.find { it.title == taskTitle }
                                            viewModel.finalFilterTarget = selectedTask?.id ?: viewModel.selectedSkillGroup
                                        }
                                        showTaskMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            //endregion

            HorizontalDivider(thickness = 1.dp, color = Color.Black.copy(alpha = 0.1f))

            //region STATS OVERVIEW

            //LEVEL BAR (Overall/Skill)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isTaskMode) {
                    val displayLevel = if (viewModel.selectedSkillGroup == "Overall") {
                        playerLevel
                    } else {
                        playerState.skills[viewModel.selectedSkillGroup]?.level ?: 1
                    }

                    val displayXp = if (viewModel.selectedSkillGroup == "Overall") {
                        xpIntoLevel.toLong()
                    } else {
                        playerState.skills[viewModel.selectedSkillGroup]?.xp?.toLong() ?: 0L
                    }

                    val reqXp = if (viewModel.selectedSkillGroup == "Overall") {
                        xpRequiredForLevel.toLong()
                    } else {
                        RpgEngine.getXpRequiredForLevel(displayLevel).toLong()
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Level $displayLevel", style = MaterialTheme.typography.labelLarge)
                            Text("$displayXp", style = MaterialTheme.typography.labelSmall)
                        }
                        LinearProgressIndicator(
                            progress = { if (reqXp > 0) displayXp.toFloat() / reqXp else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CutCornerShape(4.dp)),
                            color = Color(0xFF3D8CD1),
                            trackColor = Color.Black.copy(alpha = 0.05f),
                            strokeCap = StrokeCap.Butt,
                            gapSize = 0.dp,
                            drawStopIndicator = {}
                        )
                    }
                }
            }

            //STATISTICS GRID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatMiniCard(
                    label = "Quests Completed",
                    value = questsCompleted.toString(),
                    modifier = Modifier.weight(1f)
                )

                val unitLabel = when {
                    isTaskMode -> selectedTask.unit
                    viewModel.selectedSkillGroup != "Overall" -> "Units"
                    else -> "Actions"
                }

                StatMiniCard(
                    label = "Total $unitLabel",
                    value = totalAmountLogged.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            //Total Experience Gained
            StatMiniCard(
                label = "Experience Gained",
                value = "+$totalXpGained",
                modifier = Modifier.fillMaxWidth()
            )
            //endregion


            // HEATMAP SECTION
            Text("Calendar", style = MaterialTheme.typography.titleMedium)
            StatsHeatmap(
                repository = repository,
                filterId = viewModel.finalFilterTarget
            )

            // BAR CHART PLACEHOLDER
            Text("Activity", style = MaterialTheme.typography.titleMedium)
            StatsGraph(
                history = history,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
fun StatsHeatmap(repository: GameRepository, filterId: String) {
    var activityDates by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    val today = LocalDate.now()
    val monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM")
    val daysSinceMonday = today.dayOfWeek.value - 1
    val currentWeekMonday = today.minusDays(daysSinceMonday.toLong())
    val gridStartDate = currentWeekMonday.minusWeeks(13)

    LaunchedEffect(filterId) {
        activityDates = repository.getActivityDates(filterId, 98).toSet()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        //MONTH LABELS ROW
        Row(modifier = Modifier.padding(start = 30.dp)) {
            repeat(14) { weekIndex ->
                val weekMonday = gridStartDate.plusWeeks(weekIndex.toLong())
                val label = if (weekMonday.dayOfMonth <= 7) weekMonday.format(monthFormatter) else ""

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.sp,
                    modifier = Modifier.width(16.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            //DAY LABEL COLUMN
            Column(
                modifier = Modifier.width(26.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Mon", "", "Wed", "", "Fri", "", "Sun").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 9.sp,
                        modifier = Modifier.height(12.dp),
                        color = Color.Black.copy(alpha = 0.5f)
                    )
                }
            }

            //HEATMAP GRID
            repeat(14) { weekIndex ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { dayIndex ->
                        //Calculate date
                        val date = gridStartDate.plusWeeks(weekIndex.toLong()).plusDays(dayIndex.toLong())

                        val isFuture = date.isAfter(today)
                        val hasActivity = activityDates.contains(date)

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = when {
                                        isFuture -> Color.Transparent
                                        hasActivity -> Color.Black.copy(alpha = 0.8f)
                                        else -> Color.Black.copy(alpha = 0.1f)
                                    },
                                    shape = CutCornerShape(1.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsGraph(
    history: List<com.jwsulzen.habitrpg.data.model.CompletionRecord>,
    modifier: Modifier = Modifier
) {
    //Time range state
    var selectedRange by remember { mutableStateOf("Month") }
    val ranges = listOf("Month", "3 Months", "Year")

    //Data processing
    val chartData = remember(history, selectedRange) {
        val today = LocalDate.now()
        val startDate = when (selectedRange) {
            "Month" -> today.minusMonths(1)
            "3 Months" -> today.minusMonths(3)
            else -> today.minusYears(1)
        }

        //Filter history to range
        val filtered = history.filter { !it.date.isBefore(startDate) }

        //Grouping and Aggregation Logic
        when (selectedRange) {
            "Month" -> {
                (0..30).map { i ->
                    val d = today.minusDays(i.toLong())
                    filtered.filter { it.date == d }.sumOf { it.progressAmount }.toFloat()
                }.reversed()
            }
            "3 Months" -> {
                (0..45).map { i ->
                    val d1 = today.minusDays((i * 2).toLong())
                    val d2 = today.minusDays((i * 2 + 1).toLong())
                    filtered.filter { it.date == d1 || it.date == d2 }.sumOf { it.progressAmount }.toFloat()
                }.reversed()
            }
            else -> {
                (0..72).map { i ->
                    val chunk = (0..4).map { today.minusDays((i *  5 + it).toLong()) }
                    filtered.filter { it.date in chunk }.sumOf { it.progressAmount }.toFloat()
                }.reversed()
            }
        }
    }

    val maxVal = (chartData.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        //Range Selectors
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ranges.forEach { range ->
                OutlinedButton(
                    onClick = { selectedRange = range },
                    shape = CutCornerShape(4.dp),
                    modifier = Modifier.height(32.dp).weight(1f),
                    colors = if (selectedRange == range) {
                        ButtonDefaults.outlinedButtonColors(containerColor = Color.Black.copy(alpha = 0.05f))
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text(range, fontSize = 10.sp)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            //Y-AXIS LABELS
            Column(
                modifier = Modifier.fillMaxHeight().width(24.dp).padding(top = 8.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(text = maxVal.toInt().toString(), fontSize = 9.sp, color = Color.Gray)
                Text(text = (maxVal / 2).toInt().toString(), fontSize = 9.sp, color = Color.Gray)
                Text(text = "0", fontSize = 9.sp, color = Color.Gray)
            }

            Spacer(Modifier.width(4.dp))


            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                //GRAPH CANVAS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.Black.copy(alpha = 0.03f), CutCornerShape(8.dp))
                        .padding(8.dp)
                        .drawBehind {
                            val barCount = chartData.size
                            val spacing = 2.dp.toPx()
                            val barWidth = (size.width - (spacing * (barCount - 1))) / barCount

                            chartData.forEachIndexed { index, value ->
                                val barHeight = (value / maxVal) * size.height
                                val xOffset = index * (barWidth + spacing)

                                drawRect(
                                    color = if (value > 0) Color(0xFF3D8CD1) else Color.Gray.copy(alpha = 0.2f),
                                    topLeft = androidx.compose.ui.geometry.Offset(xOffset, size.height - barHeight),
                                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                                )
                            }
                        }
                )

                //X-AXIS LABELS
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val xLabels = when (selectedRange) {
                        "Month" -> listOf("-30", "-20", "-10", "0")
                        "3 Months" -> listOf("-90", "-60", "-30", "0")
                        else -> listOf("-1y", "-8m", "-4m", "0")
                    }
                    xLabels.forEach { label ->
                        Text(text = label, fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun StatMiniCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.03f), CutCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Black.copy(alpha = 0.5f))
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}