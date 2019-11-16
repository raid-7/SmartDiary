package ru.raid.smartdiary.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: Record)

    @Query("SELECT * FROM records")
    fun getAll(): LiveData<List<Record>>

    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): Record?
}
