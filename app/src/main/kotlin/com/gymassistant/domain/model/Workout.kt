package com.gymassistant.domain.model

import java.time.LocalDateTime

data class Workout(
    val id: Long = 0,
    val name: String = "",
    val startTime: LocalDateTime = LocalDateTime.now(),
    val endTime: LocalDateTime? = null,
    val notes: String = ""
) {
    val duration: Long?
        get() = endTime?.let {
            java.time.Duration.between(startTime, it).toMinutes()
        }

    val isActive: Boolean
        get() = endTime == null
}
