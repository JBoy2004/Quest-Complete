package com.jwsulzen.habitrpg.data.model

enum class Difficulty(val baseXp : Int) {
    TRIVIAL(20),
    EASY(40),
    MEDIUM(70),
    HARD(100)
}