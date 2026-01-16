package com.jwsulzen.habitrpg.domain

import kotlin.math.pow

object RpgEngine {
    //TWEAK NUMBERS TO ADJUST PROGRESSION SPEED
    //TODO Overall level and skill levels share the same equation right now-- make overall level more exponential?
    private const val BASE_XP = 100
    private const val SCALING_FACTOR = 1.5

    /**
     * Total XP required to reach the START of a specific level.
     * Level 1: 0 XP
     * Level 2: 100 XP
     * Level 3: 250 XP (100 + 150)
     */
    fun getTotalXpForLevel(level: Int): Int {
        if (level <= 1) return 0
        var total = 0
        for (i in 1 until level) {
            total += (BASE_XP * SCALING_FACTOR.pow((i - 1).toDouble())).toInt()
        }
        return total
    }

    //Calculates level based on total accumulated XP
    fun getLevelFromTotalXp(totalXp: Int): Int {
        var level = 1
        while (totalXp >= getTotalXpForLevel(level + 1)) {
            level++
        }
        return level
    }

    //Return a 0.0 to 1.0 progress of how far through current level the player is
    fun getLevelProgress(totalXp : Int): Float {
        val currentLevel = getLevelFromTotalXp(totalXp)
        val levelStart = getTotalXpForLevel(currentLevel)
        val levelEnd = getTotalXpForLevel(currentLevel + 1)

        val xpInThisLevel = totalXp - levelStart
        val xpRequiredForThisLevel = levelEnd - levelStart

        return (xpInThisLevel.toFloat() / xpRequiredForThisLevel.toFloat()).coerceIn(0f, 1f)
    }

    fun getXpIntoLevel(totalXp: Int): Int {
        return totalXp - getTotalXpForLevel(getLevelFromTotalXp(totalXp))
    }

    fun getXpRequiredForLevel(level: Int): Int {
        return getTotalXpForLevel(level + 1) - getTotalXpForLevel(level)
    }
}