package ru.raid.smartdiary.db

import android.graphics.BitmapFactory
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(tableName = "notes")
class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    val text: String,
    val imagePath: String,
    val date: Long
) {
    @get:Ignore
    @delegate:Ignore
    val bitmap by lazy {
        BitmapFactory.decodeFile(imagePath)
    }
}
