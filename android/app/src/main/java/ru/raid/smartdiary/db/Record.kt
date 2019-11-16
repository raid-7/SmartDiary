package ru.raid.smartdiary.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "records")
class Record(
        @PrimaryKey(autoGenerate = true) val id: Long,
        val soundPath: String,
        val date: Long
)
