package ru.raid.smartdiary.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: Record): Long

    @Update
    suspend fun update(record: Record)

    @Delete
    suspend fun delete(record: Record)

    @Query("SELECT * FROM records")
    fun getAll(): LiveData<List<Record>>

    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): Record?
}
