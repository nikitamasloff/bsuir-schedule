package com.nikitamaslov.bsuirschedule.data.local.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.nikitamaslov.bsuirschedule.data.model.Auditory

@Dao
interface AuditoryDao: BaseDao<Auditory> {

    @Query("SELECT * FROM auditory")
    fun queryAll(): List<Auditory>

    @Query("SELECT * FROM auditory WHERE number LIKE :number")
    fun queryByNumber(number: String): Auditory?

    @Query("SELECT * FROM auditory WHERE number IN (:numbers)")
    fun queryByNumber(vararg numbers: String): List<Auditory>

    @Query("DELETE FROM auditory")
    fun deleteAll()

}