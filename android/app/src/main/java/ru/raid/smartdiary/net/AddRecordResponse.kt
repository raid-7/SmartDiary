package ru.raid.smartdiary.net

import androidx.room.Ignore

data class AddRecordResponse(
        val score: Float,
        val neutrality: Float,
        val happiness: Float,
        val sadness: Float,
        val anger: Float,
        val fear: Float,
        val avatar_level: Int,
        val text: String
) {
    @get:Ignore
    val normalizedAnger: Float
        get() = anger * (1.0f - score)
}
