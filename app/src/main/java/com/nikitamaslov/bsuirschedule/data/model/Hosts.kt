package com.nikitamaslov.bsuirschedule.data.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.nikitamaslov.bsuirschedule.data.remote.exclude.Exclude
import com.google.gson.annotations.SerializedName

sealed class Host

@Entity(tableName = "groups")
class Group : Host() {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "group_primaryKey")
    var primaryKey: Int? = null

    @SerializedName("name")
    @ColumnInfo(name = "group_number")
    var number: String? = null

    @SerializedName("id")
    @ColumnInfo(name = "group_id")
    var id: Int? = null

    @SerializedName("course")
    @ColumnInfo(name = "group_course")
    var course: Int? = null

}


@Entity(tableName = "employees")
class Employee : Host() {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "employee_primaryKey")
    var primaryKey: Int? = null

    @SerializedName("firstName")
    @ColumnInfo(name = "employee_name")
    var name: String? = null

    @SerializedName("lastName")
    @ColumnInfo(name = "employee_surname")
    var surname: String? = null

    @SerializedName("middleName")
    @ColumnInfo(name = "employee_patronymic")
    var patronymic: String? = null

    @SerializedName("rank")
    @ColumnInfo(name = "employee_rank")
    var rank: String? = null

    @SerializedName("photoLink")
    @ColumnInfo(name = "employee_photoLink")
    var photoLink: String? = null

    @SerializedName("academicDepartment")
    @ColumnInfo(name = "employee_department")
    var department: List<String> = emptyList()

    @SerializedName("id")
    @ColumnInfo(name = "employee_id")
    var id: Int? = null

    @SerializedName("fio")
    @ColumnInfo(name = "employee_fio")
    var fio: String? = null

}