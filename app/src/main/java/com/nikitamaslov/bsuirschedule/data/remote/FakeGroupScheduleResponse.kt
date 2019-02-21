package com.nikitamaslov.bsuirschedule.data.remote

import android.graphics.Color
import com.nikitamaslov.bsuirschedule.data.model.Employee
import com.nikitamaslov.bsuirschedule.data.model.Group
import com.nikitamaslov.bsuirschedule.data.model.GroupSchedule

private val group1 = Group().apply {
    number = "750702"
    course = 2
    id = 22478
}

private val employee1 = Employee().apply {
    fio = "some_fio1"
    name = "some_name1"
    surname = "some_surname1"
    id = 111111
    department = listOf("some department1")
}

private val employee2 = Employee().apply {
    fio = "some_fio2"
    name = "some_name2"
    surname = "some_surname2"
    id = 222222
    department = listOf("some department2")
}

val fakeGroupScheduleResponse: Array<GroupSchedule> = arrayOf(

    GroupSchedule().apply {
        employee = employee1
        group = group1
        color = Color.parseColor("#88AAFF")
        dayOfWeek = "Понедельник"
        numberOfWeek = listOf(1,3)
        subgroup = 2
        auditory = listOf("803-2")
        startTime = "08:00"
        endTime = "09:45"
        subject = "Annn"
        note = "some note here"
        lessonType = "ЛК"
    },

    GroupSchedule().apply {
        employee = employee1
        group = group1
        color = Color.parseColor("#88A1FF")
        dayOfWeek = "Вторник"
        numberOfWeek = listOf(1)
        subgroup = 2
        auditory = listOf("456-7")
        startTime = "10:00"
        endTime = "11:45"
        subject = "Annn"
        note = "some note here"
        lessonType = "ПЗ"
    },

    GroupSchedule().apply {
        employee = employee1
        group = group1
        color = Color.parseColor("#88A2FF")
        dayOfWeek = "Среда"
        numberOfWeek = listOf(2)
        subgroup = 2
        auditory = listOf("605-2")
        startTime = "12:00"
        endTime = "13:45"
        subject = "GER"
        note = "some note here"
        lessonType = "ЛР"
    },

    GroupSchedule().apply {
        employee = employee1
        group = group1
        color = Color.parseColor("#88A3FF")
        dayOfWeek = "Четверг"
        numberOfWeek = listOf(1,2,3)
        subgroup = 2
        auditory = listOf("605-2")
        startTime = "18:00"
        endTime = "19:45"
        subject = "GER"
        note = "some note here"
        lessonType = "ЛР"
    },

    GroupSchedule().apply {
        employee = employee1
        group = group1
        color = Color.parseColor("#88A4FF")
        dayOfWeek = "Пятница"
        numberOfWeek = listOf(0,1,2,3,4)
        subgroup = 2
        auditory = listOf("605-2")
        startTime = "20:00"
        endTime = "21:45"
        subject = "ABS"
        note = "some note here"
        lessonType = "ПЗ"
    },

    GroupSchedule().apply {
        employee = employee1
        group = group1
        color = Color.parseColor("#88A5FF")
        dayOfWeek = "Суббота"
        numberOfWeek = listOf(1,3)
        subgroup = 2
        auditory = listOf("605-2")
        startTime = "08:00"
        endTime = "09:45"
        subject = "КПиЯП"
        note = "some note here"
        lessonType = "ЛК"
    },

    GroupSchedule().apply {
        employee = employee2
        group = group1
        color = Color.parseColor("#75A6BF")
        dayOfWeek = "Воскресенье"
        numberOfWeek = listOf(2,4)
        subgroup = 2
        auditory = listOf("709-4")
        startTime = "10:00"
        endTime = "11:45"
        subject = "БЖЧ"
        note = "some note here"
        lessonType = "ПЗ"
    },

    GroupSchedule().apply {
        employee = employee1
        group = group1
        color = Color.parseColor("#6973FF")
        dayOfWeek = "Понедельник"
        numberOfWeek = listOf(4)
        subgroup = 2
        auditory = listOf("105-1")
        startTime = "12:00"
        endTime = "13:45"
        subject = "физра"
        note = "some note here"
        lessonType = "ПЗ"
    }

)