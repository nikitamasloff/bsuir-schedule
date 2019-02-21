package com.nikitamaslov.bsuirschedule.data.local.database

import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nikitamaslov.bsuirschedule.data.local.database.dao.*
import com.nikitamaslov.bsuirschedule.data.model.*
import com.nikitamaslov.bsuirschedule.utils.extension.closeable
import kotlin.reflect.KClass

@android.arch.persistence.room.Database(
    entities = [
        Group::class,
        Employee::class,
        Auditory::class,
        GroupSchedule::class,
        EmployeeSchedule::class
    ], version = 1,
    exportSchema = false)
@TypeConverters(Database.TypeConverters::class)
abstract class Database : RoomDatabase() {

    companion object Factory {

        private const val DATABASE_FILE_NAME = "database.db"

        @JvmStatic
        private var instance: Database? = null

        @JvmStatic
        private fun instance(context: Context): Database =
            instance ?: synchronized(Database::class) {
                Room.databaseBuilder(
                        context.applicationContext,
                        Database::class.java,
                        DATABASE_FILE_NAME
                )
                        .build()
                        .also { instance = it }
            }

        @JvmStatic
        private fun destroyInstance() {
            instance = null
        }

        fun proxy(context: Context): Proxy = Proxy(context)

    }

    protected abstract fun groupDao(): GroupDao
    protected abstract fun employeeDao(): EmployeeDao
    protected abstract fun groupScheduleDao(): GroupScheduleDao
    protected abstract fun employeeScheduleDao(): EmployeeScheduleDao
    protected abstract fun auditoryDao(): AuditoryDao

    @Suppress("UNUSED_PARAMETER", "unused")
    class Proxy (context: Context) {

        private val database by closeable(::closed) { instance(context) }
        private var closed: Boolean = false

        fun close() {
            closed = true
            destroyInstance()
        }

        fun insert(obj: Group) = database.groupDao().insert(obj)

        fun insert(obj: Employee) = database.employeeDao().insert(obj)

        fun insert(obj: GroupSchedule) = database.groupScheduleDao().insert(obj)

        fun insert(obj: EmployeeSchedule) = database.employeeScheduleDao().insert(obj)

        fun insert(obj: Auditory) = database.auditoryDao().insert(obj)

        fun insert(vararg items: Employee) = database.employeeDao().insert(*items)

        fun insert(vararg items: EmployeeSchedule) = database.employeeScheduleDao().insert(*items)

        fun insert(vararg items: Group) = database.groupDao().insert(*items)

        fun insert(vararg items: GroupSchedule) = database.groupScheduleDao().insert(*items)

        fun insert(vararg items: Auditory) = database.auditoryDao().insert(*items)


        @JvmName("queryAllGroup")
        fun query(kClass: KClass<Group>): List<Group> = database.groupDao().queryAll()

        @JvmName("queryAllEmployee")
        fun query(kClass: KClass<Employee>): List<Employee> = database.employeeDao().queryAll()

        @JvmName("queryAllGroupSchedule")
        fun query(kClass: KClass<GroupSchedule>): List<GroupSchedule> =
            database.groupScheduleDao().queryAll()

        @JvmName("queryAllEmployeeSchedule")
        fun query(kClass: KClass<EmployeeSchedule>): List<EmployeeSchedule> =
            database.employeeScheduleDao().queryAll()

        @JvmName("queryAllAuditory")
        fun query(kClass: KClass<Auditory>): List<Auditory> = database.auditoryDao().queryAll()

        @JvmName("queryByIdGroup")
        fun queryById(kClass: KClass<Group>, id: Int): Group? = database.groupDao().queryById(id)

        @JvmName("queryByIdGroup")
        fun queryById(kClass: KClass<Group>, vararg ids: Int): List<Group> =
            database.groupDao().queryById(*ids)

        @JvmName("queryByIdEmployee")
        fun queryById(kClass: KClass<Employee>, id: Int): Employee? =
            database.employeeDao().queryById(id)

        @JvmName("queryByIdEmployee")
        fun queryById(kClass: KClass<Employee>, vararg ids: Int): List<Employee> =
            database.employeeDao().queryById(*ids)

        @JvmName("queryByIdEmployeeSchedule")
        fun queryById(kClass: KClass<EmployeeSchedule>, id: Int): List<EmployeeSchedule> =
            database.employeeScheduleDao().queryById(id)

        @JvmName("queryByIdEmployeeSchedule")
        fun queryById(kClass: KClass<EmployeeSchedule>, vararg ids: Int): List<EmployeeSchedule> =
            database.employeeScheduleDao().queryById(*ids)

        @JvmName("queryByIdGroupSchedule")
        fun queryById(kClass: KClass<GroupSchedule>, id: Int): List<GroupSchedule> =
            database.groupScheduleDao().queryById(id)

        @JvmName("queryByIdGroupSchedule")
        fun queryById(kClass: KClass<GroupSchedule>, vararg ids: Int): List<GroupSchedule> =
            database.groupScheduleDao().queryById(*ids)

        fun queryByNumber(kClass: KClass<Auditory>, number: String): Auditory? =
            database.auditoryDao().queryByNumber(number)

        fun queryByNumber(kClass: KClass<Auditory>, vararg numbers: String): List<Auditory> =
            database.auditoryDao().queryByNumber(*numbers)


        fun update(obj: Group) = database.groupDao().update(obj)

        fun update(obj: Employee) = database.employeeDao().update(obj)

        fun update(obj: GroupSchedule) = database.groupScheduleDao().update(obj)

        fun update(obj: EmployeeSchedule) = database.employeeScheduleDao().update(obj)

        fun update(obj: Auditory) = database.auditoryDao().update(obj)

        fun update(vararg items: Employee) = database.employeeDao().update(*items)

        fun update(vararg items: EmployeeSchedule) = database.employeeScheduleDao().update(*items)

        fun update(vararg items: Group) = database.groupDao().update(*items)

        fun update(vararg items: GroupSchedule) = database.groupScheduleDao().update(*items)

        fun update(vararg items: Auditory) = database.auditoryDao().update(*items)


        fun delete(obj: Group) = database.groupDao().delete(obj)

        fun delete(obj: Employee) = database.employeeDao().delete(obj)

        fun delete(obj: GroupSchedule) = database.groupScheduleDao().delete(obj)

        fun delete(obj: EmployeeSchedule) = database.employeeScheduleDao().delete(obj)

        fun delete(obj: Auditory) = database.auditoryDao().delete(obj)

        fun delete(vararg items: Employee) = database.employeeDao().delete(*items)

        fun delete(vararg items: EmployeeSchedule) = database.employeeScheduleDao().delete(*items)

        fun delete(vararg items: Group) = database.groupDao().delete(*items)

        fun delete(vararg items: GroupSchedule) = database.groupScheduleDao().delete(*items)

        fun delete(vararg items: Auditory) = database.auditoryDao().delete(*items)

        @JvmName("deleteAllGroup")
        fun deleteAll(kClass: KClass<Group>) = database.groupDao().deleteAll()

        @JvmName("deleteAllEmployee")
        fun deleteAll(kClass: KClass<Employee>) = database.employeeDao().deleteAll()

        @JvmName("deleteAllEmployeeSchedule")
        fun deleteAll(kClass: KClass<EmployeeSchedule>) = database.employeeScheduleDao().deleteAll()

        @JvmName("deleteAllGroupSchedule")
        fun deleteAll(kClass: KClass<GroupSchedule>) = database.groupScheduleDao().deleteAll()

        @JvmName("deleteAllAuditory")
        fun deleteAll(kClass: KClass<Auditory>) = database.auditoryDao().deleteAll()

        @JvmName("deleteByIdGroupSchedule")
        fun deleteById(kClass: KClass<GroupSchedule>, id: Int) =
            database.groupScheduleDao().deleteById(id)

        @JvmName("deleteByIdGroupSchedule")
        fun deleteById(kClass: KClass<GroupSchedule>, vararg ids: Int) =
            database.groupScheduleDao().deleteById(*ids)

        @JvmName("deleteByIdEmployeeSchedule")
        fun deleteById(kClass: KClass<EmployeeSchedule>, id: Int) =
            database.groupScheduleDao().deleteById(id)

        @JvmName("deleteByIdEmployeeSchedule")
        fun deleteById(kClass: KClass<EmployeeSchedule>, vararg ids: Int) =
            database.groupScheduleDao().deleteById(*ids)


        @JvmName("cachedGroups")
        fun cached(kClass: KClass<GroupSchedule>): List<Group> =
                database.groupScheduleDao().cachedGroups()

        @JvmName("cachedEmployees")
        fun cached(kClass: KClass<EmployeeSchedule>): List<Employee> =
                database.employeeScheduleDao().cachedEmployees()

        @JvmName("subjectsOfGroupSchedule")
        fun subjects(kClass: KClass<GroupSchedule>, id: Int): List<String> =
                database.groupScheduleDao().groupSubjectsById(id)

        @JvmName("subjectsOfGroupSchedule")
        fun subjects(kClass: KClass<GroupSchedule>, vararg ids: Int): List<String> =
                database.groupScheduleDao().groupSubjectsById(*ids)

        @JvmName("subjectsOfEmployeeSchedule")
        fun subjects(kClass: KClass<EmployeeSchedule>, id: Int): List<String> =
                database.employeeScheduleDao().employeeSubjectsById(id)

        @JvmName("subjectsOfEmployeeSchedule")
        fun subjects(kClass: KClass<EmployeeSchedule>, vararg ids: Int): List<String> =
                database.employeeScheduleDao().employeeSubjectsById(*ids)

    }

    object TypeConverters {

        private val gson: Gson by lazy { Gson() }

        @JvmStatic
        @TypeConverter
        fun fromStringList(list: List<String>): String = gson.toJson(list, object : TypeToken<List<String>>(){}.type)

        @JvmStatic
        @TypeConverter
        fun toStringList(json: String): List<String> = gson.fromJson(json, object : TypeToken<List<String>>(){}.type)

        @JvmStatic
        @TypeConverter
        fun fromIntList(list: List<Int>): String = gson.toJson(list, object : TypeToken<List<Int>>(){}.type)

        @JvmStatic
        @TypeConverter
        fun toIntList(json: String): List<Int> = gson.fromJson(json, object : TypeToken<List<Int>>(){}.type)

    }

}