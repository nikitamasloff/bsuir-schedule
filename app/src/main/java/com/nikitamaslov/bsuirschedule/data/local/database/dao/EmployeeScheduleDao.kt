package com.nikitamaslov.bsuirschedule.data.local.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.nikitamaslov.bsuirschedule.data.model.Employee
import com.nikitamaslov.bsuirschedule.data.model.EmployeeSchedule

@Dao
interface EmployeeScheduleDao: BaseDao<EmployeeSchedule> {

    @Query("SELECT * FROM employeeSchedules")
    fun queryAll(): List<EmployeeSchedule>

    @Query("SELECT * FROM employeeSchedules WHERE employee_id LIKE :id")
    fun queryById(id: Int): List<EmployeeSchedule>

    @Query("SELECT * FROM employeeSchedules WHERE employee_id IN (:ids)")
    fun queryById(vararg ids: Int): List<EmployeeSchedule>

    @Query("DELETE FROM employeeSchedules WHERE employee_id LIKE :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM employeeSchedules WHERE employee_id IN (:ids)")
    fun deleteById(vararg ids: Int)

    @Query("DELETE FROM employeeSchedules")
    fun deleteAll()

    @Query("SELECT DISTINCT employee_id FROM employeeSchedules WHERE employee_id IS NOT NULL")
    fun employeeIds(): List<Int>

    @Query("SELECT DISTINCT subject FROM employeeSchedules WHERE employee_id LIKE :id AND subject IS NOT NULL")
    fun employeeSubjectsById(id: Int): List<String>

    @Query("SELECT DISTINCT subject FROM employeeSchedules WHERE employee_id IN (:ids) AND subject IS NOT NULL")
    fun employeeSubjectsById(vararg ids: Int): List<String>

    @Query("SELECT employee_primaryKey, employee_id, employee_department," +
            " employee_fio, employee_name, employee_patronymic, employee_photoLink, " +
            "employee_rank, employee_surname FROM employeeSchedules")
    fun cachedEmployees(): List<Employee>

}