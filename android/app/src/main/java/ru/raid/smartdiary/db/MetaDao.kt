package ru.raid.smartdiary.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class MetaDao {
    @Insert
    abstract suspend fun insert(data: Metadata)

    @Query("SELECT * FROM metadata WHERE name = :name LIMIT 1")
    abstract suspend fun get(name: String): Metadata?

    open suspend fun getMeta(name: String): String? = get(name)?.value

    open suspend fun insert(name: String, value: String) = insert(Metadata(name, value))

    @Transaction
    open suspend fun atomicGet(name: String, constr: () -> String?): String? {
        var res = getMeta(name)
        if (res != null)
            return res
        res = constr()
        if (res != null)
            insert(name, res)
        return res
    }
}
