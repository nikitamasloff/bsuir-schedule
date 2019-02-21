package com.nikitamaslov.bsuirschedule.data.local.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.nikitamaslov.bsuirschedule.data.model.Group

@Dao
interface GroupDao: BaseDao<Group> {

    @Query("SELECT * FROM groups")
    fun queryAll(): List<Group>

    @Query("SELECT * FROM groups WHERE group_id LIKE :id")
    fun queryById(id: Int): Group?

    @Query("SELECT * FROM groups WHERE group_id IN (:ids)")
    fun queryById(vararg ids: Int): List<Group>

    @Query("DELETE FROM groups")
    fun deleteAll()

}