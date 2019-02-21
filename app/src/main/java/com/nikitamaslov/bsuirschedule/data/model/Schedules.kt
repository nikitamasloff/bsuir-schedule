package com.nikitamaslov.bsuirschedule.data.model

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.RoomWarnings
import com.google.gson.annotations.SerializedName
import com.nikitamaslov.bsuirschedule.data.remote.exclude.Exclude

sealed class Schedule

@Entity(tableName = "groupSchedules")
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
class GroupSchedule : Schedule() {

    @SerializedName("dont_serialize1")
    //@Exclude
    @PrimaryKey(autoGenerate = true)
    var primaryKey: Int? = null

    //@Exclude
    @SerializedName("dont_serialize2")
    @Embedded
    var employee: Employee? = null

    //@Exclude
    @SerializedName("dont_serialize3")
    @Embedded
    var group: Group? = null

    @Exclude
    var color: Int? = null

    @SerializedName("weekDay")
    var dayOfWeek: String? = null

    @SerializedName("weekNumber")
    var numberOfWeek: List<Int> = emptyList()

    @SerializedName("numSubgroup")
    var subgroup: Int? = null

    @SerializedName("auditory")
    var auditory: List<String> = emptyList()

    @SerializedName("startLessonTime")
    var startTime: String? = null

    @SerializedName("endLessonTime")
    var endTime: String? = null

    @SerializedName("subject")
    var subject: String? = null

    @SerializedName("note")
    var note: String? = null

    @SerializedName("lessonType")
    var lessonType: String? = null

}

@Entity(tableName = "employeeSchedules")
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
class EmployeeSchedule : Schedule() {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("dont_serialize1")
    var primaryKey: Int? = null

    //@Exclude
    @SerializedName("dont_serialize2")
    @Embedded
    var employee: Employee? = null

    //@Exclude
    @SerializedName("dont_serialize3")
    var group: List<String> = emptyList()

    @Exclude
    var color: Int? = null

    @SerializedName("weekDay")
    var dayOfWeek: String? = null

    @SerializedName("weekNumber")
    var numberOfWeek: List<Int> = emptyList()

    @SerializedName("subgroup")
    var subgroup: Int? = null

    @SerializedName("auditory")
    var auditory: List<String> = emptyList()

    @SerializedName("startLessonTime")
    var startTime: String? = null

    @SerializedName("endLessonTime")
    var endTime: String? = null

    @SerializedName("subject")
    var subject: String? = null

    @SerializedName("note")
    var note: String? = null

    @SerializedName("lessonType")
    var lessonType: String? = null

}