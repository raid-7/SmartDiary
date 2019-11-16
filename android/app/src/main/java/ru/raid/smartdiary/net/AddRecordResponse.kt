package ru.raid.smartdiary.net

data class AddRecordResponse(
        val score: Float,
        val neutrality: Float,
        val happiness: Float,
        val sadness: Float,
        val anger: Float,
        val fear: Float
)
