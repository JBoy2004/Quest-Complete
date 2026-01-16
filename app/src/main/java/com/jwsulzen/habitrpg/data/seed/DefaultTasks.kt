package com.jwsulzen.habitrpg.data.seed

import com.jwsulzen.habitrpg.data.model.Task
import com.jwsulzen.habitrpg.data.model.Schedule
import com.jwsulzen.habitrpg.data.model.Difficulty

object DefaultTasks {
    val tasks = listOf(
        Task(
            id = "task_read_20",
            title = "Read for 20 minutes",
            description = null,
            skillId = "reading_id",
            /*schedule = Schedule.Daily,*/
            difficulty = Difficulty.EASY,
            isCustom = false
        ),
        Task(
            id = "task_write_20",
            title = "Write for 20 minutes",
            description = null,
            skillId = "writing_id",
            /*schedule = Schedule.Daily,*/
            difficulty = Difficulty.MEDIUM,
            isCustom = false
        ),
        Task(
            id = "task_program_20",
            title = "Program for 20 minutes",
            description = null,
            skillId = "programming_id",
            /*schedule = Schedule.Daily,*/
            difficulty = Difficulty.MEDIUM,
            isCustom = false
        ),
        Task(
            id = "task_push_ups_20",
            title = "Do 20 push-ups",
            description = null,
            skillId = "fitness_id",
            /*schedule = Schedule.Daily,*/
            difficulty = Difficulty.MEDIUM,
            isCustom = false
        ),
        Task(
            id = "task_study_20",
            title = "Study for 20 minutes",
            description = null,
            skillId = "studying_id",
            /*schedule = Schedule.Daily,*/
            difficulty = Difficulty.MEDIUM,
            isCustom = false
        ),
        Task(
            id = "task_anime_1",
            title = "Watch 1 anime episode",
            description = null,
            skillId = "language_learning_id",
            /*schedule = Schedule.Daily,*/
            difficulty = Difficulty.EASY,
            isCustom = false
        ),
        Task(
            id = "task_sleep_7",
            title = "Sleep for 7+ hours",
            description = null,
            skillId = "sleep_id", //TODO I want to make sure I properly fill in the id, add a hint that pops up somehow?
            /*schedule = Schedule.Daily,*/
            difficulty = Difficulty.EASY,
            isCustom = false
        ),
        Task(
            id = "task_skin_care",
            title = "Skin care",
            description = null,
            skillId = "hygiene_id", //TODO I want to make sure I properly fill in the id, add a hint that pops up somehow?
            /*schedule = Schedule.Daily,*/
            difficulty = Difficulty.EASY,
            isCustom = false
        )
    )
}

//TODO make list of current tasks instead of just loading all the tasks!