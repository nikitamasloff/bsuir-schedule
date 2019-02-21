package com.nikitamaslov.bsuirschedule.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.nikitamaslov.bsuirschedule.data.model.*
import kotlin.reflect.KClass

@Suppress("UNUSED_PARAMETER", "unused", "MemberVisibilityCanBePrivate")
object MultitypeSerializer {

    private const val KEY_TYPE = "type"
    private const val KEY_VALUE = "value"

    private const val TYPE_GROUP = "type_group"
    private const val TYPE_EMPLOYEE = "type_employee"
    private const val TYPE_GROUP_SCHEDULE = "type_group_schedule"
    private const val TYPE_EMPLOYEE_SCHEDULE = "type_employee_schedule"

    private val gson: Gson by lazy { Gson() }

    fun serialize(obj: Host?): String? = obj?.let {
        when (obj) {
            is Group -> serialize(obj)
            is Employee -> serialize(obj)
        }
    }

    fun serialize(obj: Group?): String? = obj?.let {
        gson.toJson(GroupSerializeModel(it))
    }

    fun serialize(obj: Employee?): String? = obj?.let {
        gson.toJson(EmployeeSerializeModel(it))
    }

    fun serialize(obj: Schedule?): String? = obj?.let {
        when (obj) {
            is GroupSchedule -> serialize(obj)
            is EmployeeSchedule -> serialize(obj)
        }
    }

    fun serialize(obj: GroupSchedule?): String? = obj?.let {
        gson.toJson(GroupScheduleSerializeModel(it))
    }

    fun serialize(obj: EmployeeSchedule?): String? = obj?.let {
        gson.toJson(EmployeeScheduleSerializeModel(it))
    }

    @JvmName("deserializeHost")
    fun deserialize(kClass: KClass<Host>, src: String?): Host? = src?.let {
        val type: String? = gson.fromJson(src, JsonObject::class.java).get(KEY_TYPE).asString
        when (type) {
            TYPE_GROUP -> gson.fromJson(src, GroupSerializeModel::class.java).value
            TYPE_EMPLOYEE -> gson.fromJson(src, EmployeeSerializeModel::class.java).value
            null -> null
            else -> throw IllegalStateException("unknown persisted type")
        }
    }

    @JvmName("deserializeSchedule")
    fun deserialize(kClass: KClass<Schedule>, src: String?): Schedule? = src?.let {
        val type: String? = gson.fromJson(src, JsonObject::class.java).get(KEY_TYPE).asString
        when (type) {
            TYPE_GROUP_SCHEDULE -> gson.fromJson(src, GroupScheduleSerializeModel::class.java).value
            TYPE_EMPLOYEE_SCHEDULE -> gson.fromJson(src, EmployeeScheduleSerializeModel::class.java).value
            null -> null
            else -> throw IllegalStateException("unknown persisted type")
        }
    }

    private class GroupSerializeModel( @SerializedName(KEY_VALUE) val value: Group) {
        @SerializedName(KEY_TYPE)
        private val type = TYPE_GROUP
    }

    private class EmployeeSerializeModel( @SerializedName(KEY_VALUE) val value: Employee) {
        @SerializedName(KEY_TYPE)
        private val type = TYPE_EMPLOYEE
    }

    private class GroupScheduleSerializeModel( @SerializedName(KEY_VALUE) val value: GroupSchedule) {
        @SerializedName(KEY_TYPE)
        private val type = TYPE_GROUP_SCHEDULE
    }

    private class EmployeeScheduleSerializeModel( @SerializedName(KEY_VALUE) val value: EmployeeSchedule) {
        @SerializedName(KEY_TYPE)
        private val type = TYPE_EMPLOYEE_SCHEDULE
    }

}