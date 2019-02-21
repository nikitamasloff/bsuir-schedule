package com.nikitamaslov.bsuirschedule.data.local.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.nikitamaslov.bsuirschedule.data.model.Employee

@Dao
interface EmployeeDao: BaseDao<Employee> {

    @Query("SELECT * FROM employees")
    fun queryAll(): List<Employee>

    @Query("SELECT * FROM employees WHERE employee_id LIKE :id")
    fun queryById(id: Int): Employee?

    @Query("SELECT * FROM employees WHERE employee_id IN (:ids)")
    fun queryById(vararg ids: Int): List<Employee>

    @Query("DELETE FROM employees")
    fun deleteAll()

}