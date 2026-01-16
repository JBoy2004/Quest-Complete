package com.jwsulzen.habitrpg.data.model

sealed class ProgressionType {

    data class Maintenance(
        val maxLevel: Int,
        val decayAfterDays: Int
    ) : ProgressionType()

    object Growth : ProgressionType()
}