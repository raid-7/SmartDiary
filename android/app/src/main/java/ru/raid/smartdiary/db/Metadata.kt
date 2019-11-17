package ru.raid.smartdiary.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metadata")
class Metadata(
        @PrimaryKey val name: String,
        val value: String
) {
    companion object {
        const val USER_ID = "user_id"
        const val AVATAR_LEVEL = "avatar_level"
    }
}
