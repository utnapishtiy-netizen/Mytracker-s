package com.example.pixeltracker.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: (streak: Int, totalSteps: Int) -> Boolean
)

object AchievementList {
    val list = listOf(
        Achievement(
            id = "first_step",
            title = "Первый шаг",
            description = "Сделайте свои первые 1 000 шагов",
            iconName = "walk",
            isUnlocked = { _, steps -> steps >= 1000 }
        ),
        Achievement(
            id = "streak_3",
            title = "В ритме",
            description = "Удерживайте стрик 3 дня подряд",
            iconName = "fire",
            isUnlocked = { streak, _ -> streak >= 3 }
        ),
        Achievement(
            id = "streak_7",
            title = "Железная воля",
            description = "Стрик из 7 дней активных тренировок",
            iconName = "local_fire_department",
            isUnlocked = { streak, _ -> streak >= 7 }
        ),
        Achievement(
            id = "marathoner",
            title = "Марафонец",
            description = "Накопите 10 000 шагов за один день",
            iconName = "directions_run",
            isUnlocked = { _, steps -> steps >= 10000 }
        )
    )
}
