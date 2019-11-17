package ru.raid.smartdiary.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.raid.smartdiary.net.AddRecordResponse


@Entity(tableName = "records")
data class Record(
        @PrimaryKey(autoGenerate = true) val id: Long,
        val soundPath: String,
        val date: Long,
        @Embedded(prefix = "info_") val info: AddRecordResponse?
)
