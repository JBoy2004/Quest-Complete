package com.jwsulzen.habitrpg.ui.screens.tasksettings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jwsulzen.habitrpg.data.model.Difficulty
import com.jwsulzen.habitrpg.data.repository.GameRepository
import com.jwsulzen.habitrpg.data.seed.DefaultSkills
import com.jwsulzen.habitrpg.data.seed.TaskTemplates
import com.jwsulzen.habitrpg.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSettingsScreen(
    navController: NavController,
    repository: GameRepository,
    skillId: String,
    isMeasurable: Boolean,
    taskId: String?
) {
    val viewModel: TaskSettingsViewModel = viewModel(
        factory = TaskSettingsViewModel.provideFactory(repository, taskId)
    )
    var showRepeatMenu by remember { mutableStateOf(false) }
    var showDifficultyMenu by remember { mutableStateOf(false) }
    val repeatOptions = listOf("Every day", "Every week", "Every month"/*, "Custom"*/)
    val hintTask = TaskTemplates.getTemplateForSkill(skillId, isMeasurable = isMeasurable)
    val isEditMode = !taskId.isNullOrBlank()
    var showDeleteDialog by remember { mutableStateOf(false) }

    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = remember(showTimePicker) {
        TimePickerState(
            initialHour = viewModel.notificationHour,
            initialMinute = viewModel.notificationMinute,
            is24Hour = false
        )
    }

    var showDaysDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //TODO Proceed with Save
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (isEditMode) "Edit Quest" else "Create ${DefaultSkills.skills.find { it.id == skillId }?.name} Quest",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        //Contain the information for current customized task
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CutCornerShape(8.dp)
        ) {
            Column (
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //region TITLE
                OutlinedTextField(
                    value = viewModel.title,
                    onValueChange = { viewModel.title = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. ${hintTask?.title ?: "e.g. Read for 20 mins"}") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                //endregion

                //region GOAL
                OutlinedTextField(
                    value = viewModel.goal,
                    onValueChange = { if (it.all { char -> char.isDigit() }) viewModel.goal = it },
                    label = { Text("Goal") },
                    placeholder = { Text("e.g. ${hintTask?.goal?.toString() ?: "1"}") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
                //endregion

                //region UNIT
                if (isMeasurable) {
                    OutlinedTextField(
                        value = viewModel.unit,
                        onValueChange = { viewModel.unit = it },
                        label = { Text("Unit") },
                        placeholder = { Text("e.g. ${hintTask?.unit ?: "e.g. minutes"}") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                //endregion

                //region FREQUENCY + DIFFICULTY
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    //Repeat Selector
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = viewModel.repeatType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Frequency") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.fillMaxWidth(),
                            interactionSource = remember { MutableInteractionSource() }
                                .also { interactionSource ->
                                    LaunchedEffect(interactionSource) {
                                        interactionSource.interactions.collect {
                                            if (it is PressInteraction.Release) showRepeatMenu = true
                                        }
                                    }
                                }
                        )

                        //Dropdown Menu for Repeat options
                        DropdownMenu(
                            expanded = showRepeatMenu,
                            onDismissRequest = { showRepeatMenu = false },
                            modifier = Modifier.fillMaxWidth(0.45f)
                        ) {
                            repeatOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.repeatType = option
                                        showRepeatMenu = false
                                    }
                                )
                            }
                        }
                    }

                    //Difficulty Selector
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = viewModel.selectedDifficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Difficulty") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.fillMaxWidth(),
                            interactionSource = remember { MutableInteractionSource() }
                                .also { interactionSource ->
                                    LaunchedEffect(interactionSource) {
                                        interactionSource.interactions.collect {
                                            if (it is PressInteraction.Release) showDifficultyMenu = true
                                        }
                                    }
                                }
                        )

                        //Dropdown Menu for Difficulty options
                        DropdownMenu(
                            expanded = showDifficultyMenu,
                            onDismissRequest = { showDifficultyMenu = false },
                            modifier = Modifier.fillMaxWidth(0.45f)
                        ) {
                            Difficulty.entries.forEach { difficulty ->
                                DropdownMenuItem(
                                    text = { Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        viewModel.selectedDifficulty = difficulty
                                        showDifficultyMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                //endregion

                //region CUSTOM INTERVAL SUB-MENU TODO implement custom scheduling??? its gross
                if (viewModel.repeatType == "Custom") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Every ", style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(
                            value = viewModel.intervalValue,
                            onValueChange = { if (it.all { char -> char.isDigit() }) viewModel.intervalValue = it },
                            modifier = Modifier.width(80.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        Text(" days", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                //endregion

                //region NOTIFICATION
                //TIME SELECTOR
                OutlinedTextField(
                    value = if (viewModel.hasNotification) {
                        String.format("%02d:%02d", viewModel.notificationHour, viewModel.notificationMinute)
                    } else {
                        "Off"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Notification") },
                    modifier = Modifier.fillMaxWidth(),
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) showTimePicker = true
                                }
                            }
                        }
                )

                //DAYS SELECTOR (visible if time is set)
                if (viewModel.hasNotification) {
                    OutlinedTextField(
                        value = viewModel.notificationDaysShort,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Days") },
                        modifier = Modifier.fillMaxWidth(),
                        interactionSource = remember { MutableInteractionSource() }
                            .also { interactionSource ->
                                LaunchedEffect(interactionSource) {
                                    interactionSource.interactions.collect {
                                        if (it is PressInteraction.Release) showDaysDialog = true
                                    }
                                }
                            }
                    )
                }
                //endregion

                //region CREATE TASK BUTTON + DELETE TASK BUTTON (if isEditMode = true)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //DELETE TASK BUTTON
                    if (isEditMode) {
                        Button(
                            onClick = { showDeleteDialog = true },
                            shape = CutCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete Quest")
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    //CREATE TASK BUTTON
                    Button(
                        onClick = {
                            //VALIDATION LOGIC TODO FORCE ALL FIELDS TO BE FILLED
                            val finalTitle = viewModel.title.ifBlank { hintTask?.title } ?: ""
                            val finalGoal = viewModel.goal.toIntOrNull() ?: hintTask?.goal ?: 1

                            if (finalTitle.isBlank() || finalGoal < 1) {
                                // TODO Show Toast or Snack bar error message here
                                return@Button
                            }

                            //NOTIFICATION PERMISSION REQUEST
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }

                            //MAP SCHEDULE
                            val taskSchedule = when (viewModel.repeatType) {
                                "Every day" -> com.jwsulzen.habitrpg.data.model.Schedule.Daily
                                "Every week" -> com.jwsulzen.habitrpg.data.model.Schedule.Weekly(setOf(viewModel.selectedDate.dayOfWeek))
                                "Every month" -> com.jwsulzen.habitrpg.data.model.Schedule.Monthly(viewModel.selectedDate.dayOfMonth)
                                "Custom" -> com.jwsulzen.habitrpg.data.model.Schedule.Interval(
                                    everyXDays = viewModel.intervalValue.toIntOrNull() ?: 1,
                                    startDate = viewModel.selectedDate
                                )
                                else -> com.jwsulzen.habitrpg.data.model.Schedule.Daily // Fallback
                            }

                            //CREATE TASK
                            viewModel.onSaveTask(
                                skillId = skillId,
                                schedule = taskSchedule,
                                isMeasurable = isMeasurable,
                                context = context
                            )
                            navController.popBackStack(Screen.TasklistScreen.route, inclusive = false)
                        },
                        //modifier = Modifier.align(Alignment.End),
                        shape = CutCornerShape(4.dp)
                    ) {
                        Text(text = if (isEditMode) "Save Changes" else "Create Quest")
                    }
                }
                //endregion
            }
        }
    }

    //Alert Dialog for deleting task
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Are you sure you want to delete this quest?",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            shape = CutCornerShape(8.dp),
            confirmButton = {
                //DELETE
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.onDeleteTask(context) {
                            navController.popBackStack(Screen.TasklistScreen.route, inclusive = false)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CutCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                //CANCEL
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CutCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.notificationHour = timePickerState.hour
                    viewModel.notificationMinute = timePickerState.minute
                    viewModel.hasNotification = true
                    showTimePicker = false
                }) { Text("DONE") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.hasNotification = false
                    showTimePicker = false
                }) { Text("CLEAR") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    if (showDaysDialog) {
        AlertDialog(
            onDismissRequest = { showDaysDialog = false },
            title = { Text("Select Days") },
            text = {
                Column {java.time.DayOfWeek.entries.forEach { day ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.toggleNotificationDay(day)
                        }
                    ) {
                        Checkbox(
                            checked = viewModel.notificationDays.contains(day),
                            onCheckedChange = { viewModel.toggleNotificationDay(day) }
                        )
                        Text(day.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
                }
            },
            confirmButton = {
                TextButton(onClick = {showDaysDialog = false }) { Text("Done") }
            }
        )
    }
}