package com.jwsulzen.habitrpg.ui.screens.addtask

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jwsulzen.habitrpg.ui.navigation.Screen
import com.jwsulzen.habitrpg.ui.screens.tasklist.TaskItem
import com.jwsulzen.habitrpg.ui.screens.tasklist.TaskListViewModel
import com.jwsulzen.habitrpg.data.model.Difficulty
import com.jwsulzen.habitrpg.data.seed.DefaultSkills
import com.jwsulzen.habitrpg.R
import com.jwsulzen.habitrpg.data.repository.GameRepository
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    navController: NavController,
    repository: GameRepository
) {
    val viewModel: AddTaskViewModel = viewModel(
        factory = AddTaskViewModel.provideFactory(repository)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Add Task",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        //Contain the information for current customized task
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column (modifier = Modifier.padding(16.dp)) {
                //region TITLE AND DESCRIPTION INPUT
                var title by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }
                //TITLE INPUT
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    // placeholder = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                //DESCRIPTION INPUT
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    // placeholder = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                //endregion

                //region SKILL SELECTION
                val skillOptions = DefaultSkills.skills //Pull from GameRepository for user created skills in the future?
                var expanded by remember { mutableStateOf(false) }

                // Initialize with placeholder
                var selectedSkillName by remember { mutableStateOf("Select a Skill") }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedSkillName,
                        onValueChange = {},
                        readOnly = true, // Prevents typing, tap only
                        label = { Text("Target Skill") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        // Loop through skill list
                        skillOptions.forEach { skill ->
                            DropdownMenuItem(
                                text = { Text(text = skill.name) },
                                onClick = {
                                    selectedSkillName = skill.name
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
                //endregion

                //TODO make difficulty selection look okay
                //region DIFFICULTY SELECTION
                val options = Difficulty.entries
                var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }

                Text(
                    text = "Difficulty",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                ) //TODO add info button dropdown to show recommended difficulty scaling per task!

                //TODO this looks bad make it better. Probably replace with indexed buttons instead of segmentedButtonRow
                SingleChoiceSegmentedButtonRow (
                    modifier = Modifier.fillMaxWidth(),
                    space = 0.dp //removes rounded ends of entire row
                ) {
                    options.forEachIndexed { index, difficulty ->
                        SegmentedButton(
                            shape = CutCornerShape(0.dp), //perfect rectangle
                            onClick = { selectedDifficulty = difficulty },
                            selected = difficulty == selectedDifficulty,
//                            icon = {
//                                val iconRes = when(difficulty) {
//                                    Difficulty.TRIVIAL -> R.drawable.difficulty1
//                                    Difficulty.EASY -> R.drawable.difficulty2
//                                    Difficulty.MEDIUM -> R.drawable.difficulty3
//                                    Difficulty.HARD -> R.drawable.difficulty4
//                                    Difficulty.MONUMENTAL -> R.drawable.difficulty5
//                                }
//                                Icon(
//                                    painter = painterResource(id = iconRes),
//                                    contentDescription = null,
//                                    modifier = Modifier.size(18.dp)
//                                )
//                            },
                            label = {
                                // Use .name.lowercase().capitalize() for a pretty string! -- capitalize is deprecated :(
                                Text(
                                    text = difficulty.name,
                                    fontSize = 8.sp //temporary solution to make text fit
                                )
                            }
                        )
                    }
                }
                //endregion

                //TODO add scheduling information (do this later when working on scheduling logic)
                //region SCHEDULING SELECTION
                //endregion

                //TODO add check for empty skill input (show error and prevent task creation instead of generic skill type)
                //region CREATE TASK BUTTON
                Button(
                    onClick = {
                        val selectedSkill = skillOptions.find { it.name == selectedSkillName } //
                        viewModel.onAddTask(
                            title = title,
                            description = description,
                            skillId = selectedSkill?.id ?: "general_id", //failsafe to go toward general, remove?
                            difficulty = selectedDifficulty
                        )
                        navController.navigate(Screen.TasklistScreen.route) //This might break the NavBar
                        },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(size = 14.dp)
                ) {
                    Text(text = "Create")
                }
                //endregion
            }
        }
        //TODO make lazy column underneath new task card with default tasks FOR SELECTED SKILL (e.g. all default reading tasks)
        //region SUGGESTED TASKS LIST
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
//            items(tasks) { task ->
//
//            }
        }
        //endregion
    }
}