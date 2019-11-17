package ru.raid.smartdiary.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.*

@Dao
abstract class MetaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(data: Metadata)

    @Query("SELECT * FROM metadata WHERE name = :name LIMIT 1")
    abstract suspend fun get(name: String): Metadata?

    @Query("SELECT * FROM metadata WHERE name = :name LIMIT 1")
    abstract fun getLive(name: String): LiveData<Metadata?>

    open fun getLiveMeta(name: String) = Transformations.map(getLive(name)) { it?.value }

    open suspend fun getMeta(name: String) = get(name)?.value

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
