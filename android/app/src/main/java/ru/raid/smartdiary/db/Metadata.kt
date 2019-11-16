package ru.raid.smartdiary.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metadata")
class Metadata(
        @PrimaryKey val name: String,
        val value: String
)
