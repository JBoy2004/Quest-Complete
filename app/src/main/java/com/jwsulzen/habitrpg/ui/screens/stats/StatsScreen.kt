package com.jwsulzen.habitrpg.ui.screens.stats

import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jwsulzen.habitrpg.data.repository.GameRepository
import com.jwsulzen.habitrpg.domain.RpgEngine
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.setFrom
import androidx.compose.ui.graphics.vector.VectorProperty
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.jwsulzen.habitrpg.R
import com.jwsulzen.habitrpg.data.model.Difficulty
import com.jwsulzen.habitrpg.data.model.PlayerState
import com.jwsulzen.habitrpg.data.seed.DefaultSkills
import com.jwsulzen.habitrpg.ui.navigation.Screen
import com.jwsulzen.habitrpg.ui.screens.statsdetail.StatsDetailScreen
import java.time.LocalDate

private val TITLE_COLUMN_WIDTH = 220.dp
private val LEVEL_COLUMN_WIDTH = 60.dp
private val ICON_TOGGLE_WIDTH = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    repository: GameRepository
) {
    val viewModel: StatsViewModel = viewModel(
        factory = StatsViewModel.provideFactory(repository)
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

    var showSelectionMenu by remember { mutableStateOf(false) }
    val skillOptions = listOf("Overall") + playerState.skills.keys.toList()
    var selectedSkillFilter by remember { mutableStateOf("Overall") }

    var showStatsPopup by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState() //TODO make it pop all the way up?

    Surface( //TODO replace with canvas image for texture
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF1E9D2)
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .drawBehind {
                    val strokeWidth = 1.5.dp.toPx()
                    val color = Color.Black.copy(alpha = 0.1f)

                    val toggleWidthPx = ICON_TOGGLE_WIDTH.toPx()
                    val titleWidthPx = TITLE_COLUMN_WIDTH.toPx()
                    val levelWidthPx = LEVEL_COLUMN_WIDTH.toPx()

                    //VERTICAL LINE 1 (after toggle + title)
                    drawLine(
                        color = color,
                        start = androidx.compose.ui.geometry.Offset(toggleWidthPx + titleWidthPx, 0f),
                        end = androidx.compose.ui.geometry.Offset(toggleWidthPx + titleWidthPx, size.height),
                        strokeWidth = strokeWidth
                    )

                    //VERTICAL LINE 2 (after level)
                    drawLine(
                        color = color,
                        start = androidx.compose.ui.geometry.Offset(toggleWidthPx + titleWidthPx + levelWidthPx, 0f),
                        end = androidx.compose.ui.geometry.Offset(toggleWidthPx + titleWidthPx + levelWidthPx, size.height),
                        strokeWidth = strokeWidth
                    )
                },
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                //MAIN LEVEL DISPLAY
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.width(ICON_TOGGLE_WIDTH))
                    StatsProgressRow(
                        label = "Main Level",
                        icon = "🛡",
                        level = playerLevel,
                        progress = RpgEngine.getLevelProgress(currentTotalXp),
                        onClick = { navController.navigate("${Screen.StatsDetailScreen.route}/${""}/overall") }
                    )
                }
            }

            //Loop through all skills player has progress in
            items(items = playerState.skills.keys.toList()) { skillId ->
                SkillRow(
                    skillId = skillId,
                    playerState = playerState,
                    navController = navController,
                    tasks = allTasks.filter { it.skillId == skillId }
                )
            }
        }

        //region STATS BOTTOM SHEET
        if (showStatsPopup) {
            ModalBottomSheet(
                onDismissRequest = { showStatsPopup = false },
                sheetState = sheetState,
                containerColor = Color(0xFFF1E9D2),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Black.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxHeight(1f)
            ) {

            }
        }
        //endregion
    }
}

@Composable
fun StatsProgressRow(
    label: String,
    icon: String,
    level: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    showLevel: Boolean = true,
    showBar: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable { onClick() }
            .padding(vertical = 4.dp)
            .drawBehind {
                val strokeWidth = 1.5.dp.toPx()
                val color = Color.Black.copy(alpha = 0.1f)
                //HORIZONTAL BOTTOM LINE
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height + 24),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height + 24),
                    strokeWidth = strokeWidth
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        //ICON & TITLE AREA
        Row(
            modifier = Modifier.width( if(showLevel) TITLE_COLUMN_WIDTH else TITLE_COLUMN_WIDTH - 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 30.sp, modifier = Modifier.padding(horizontal = 8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        //LEVEL DISPLAY
        Box(
            modifier = Modifier.width(LEVEL_COLUMN_WIDTH),
            contentAlignment = Alignment.Center
        ) {
            if (showLevel) {
                Text(
                    text = level.toString(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        //PROGRESS BAR
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if(showBar) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CutCornerShape(2.dp)),
                    color = Color(0xFF3D8CD1),
                    trackColor = Color.Black.copy(alpha = 0.8f),
                    strokeCap = StrokeCap.Butt,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }
    }
}

@Composable
fun SkillRow(
    skillId: String,
    playerState: PlayerState,
    navController: NavController,
    tasks: List<com.jwsulzen.habitrpg.data.model.Task>
) {
    val skill = DefaultSkills.skills.find { it.id == skillId }
    val progressData = playerState.skills[skillId] ?: return
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Expand/Collapse Toggle
            Box(
                modifier = Modifier
                    .width(ICON_TOGGLE_WIDTH)
                    .fillMaxHeight()
                    .clickable { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                    )
            }

            StatsProgressRow(
                label = skill?.name ?: "Unknown",
                icon = skill?.emoji ?: "❓",
                level = progressData.level,
                progress = RpgEngine.getLevelProgress(progressData.xp),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("${Screen.StatsDetailScreen.route}/${skillId}/skill") }
            )
        }

        if (isExpanded) {
            Column(
                modifier = Modifier.padding(start = 48.dp, top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tasks.filter { it.skillId == skillId }.forEach { task ->
                    TaskRow(navController = navController, task = task)
                }
            }
        }
    }
}

@Composable
fun TaskRow(
    navController: NavController,
    task: com.jwsulzen.habitrpg.data.model.Task
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.width(ICON_TOGGLE_WIDTH))

        StatsProgressRow(
            label = task.title,
            icon = "📜",
            level = 0,
            showLevel = false,
            showBar = false,
            progress = 0f,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            onClick = { navController.navigate("${Screen.StatsDetailScreen.route}/${task.id}/task") }
        )
    }
}


//            DANGER ZONE
//            //TODO move danger zone to settings screen
//            //TODO add protection against accidental deletion
//            Spacer(Modifier.weight(1f)) //push button to bottom
//            Button(
//                onClick = { viewModel.onResetData() },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.error
//                ),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Reset All Data")
//            }