package com.gymassistant.data.local.entity

/**
 * Muscle groups based on MyFit reference
 */
enum class MuscleGroup(val displayName: String) {
    CHEST("Грудь"),
    BACK("Спина"),
    SHOULDERS("Плечи"),
    BICEPS("Бицепс"),
    TRICEPS("Трицепс"),
    FOREARMS("Предплечья"),
    QUADS("Квадрицепс"),
    HAMSTRINGS("Бицепс бедра"),
    GLUTES("Ягодицы"),
    CALVES("Икры"),
    ABS("Пресс"),
    TRAPS("Трапеции"),
    LATS("Широчайшие"),
    OTHER("Другое");

    companion object {
        fun fromString(value: String): MuscleGroup {
            return entries.find { it.name == value } ?: OTHER
        }
    }
}
