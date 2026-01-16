package com.jwsulzen.habitrpg.data.model

enum class Difficulty(val baseXp : Int) {
    TRIVIAL(5),
    EASY(10),
    MEDIUM(25),
    HARD(50),
    MONUMENTAL(100)
}