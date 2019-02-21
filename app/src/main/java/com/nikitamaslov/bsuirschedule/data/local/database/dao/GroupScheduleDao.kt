package com.nikitamaslov.bsuirschedule.data.local.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.nikitamaslov.bsuirschedule.data.model.Group
import com.nikitamaslov.bsuirschedule.data.model.GroupSchedule

@Dao
interface GroupScheduleDao : BaseDao<GroupSchedule> {

    @Query("SELECT * FROM groupSchedules")
    fun queryAll(): List<GroupSchedule>

    @Query("SELECT * FROM groupSchedules WHERE group_id LIKE :id")
    fun queryById(id: Int): List<GroupSchedule>

    @Query("SELECT * FROM groupSchedules WHERE group_id IN (:ids)")
    fun queryById(vararg ids: Int): List<GroupSchedule>

    @Query("DELETE FROM groupSchedules WHERE group_id LIKE :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM groupSchedules WHERE group_id IN (:ids)")
    fun deleteById(vararg ids: Int)

    @Query("DELETE FROM groupSchedules")
    fun deleteAll()

    @Query("SELECT DISTINCT group_id FROM groupSchedules WHERE group_id IS NOT NULL")
    fun groupIds(): List<Int>

    @Query("SELECT DISTINCT subject FROM groupSchedules WHERE group_id LIKE :id")
    fun groupSubjectsById(id: Int): List<String>

    @Query("SELECT DISTINCT subject FROM groupSchedules WHERE group_id IN (:ids)")
    fun groupSubjectsById(vararg ids: Int): List<String>

    @Query("SELECT group_primaryKey, group_id, group_number, group_course FROM groupSchedules")
    fun cachedGroups(): List<Group>

}