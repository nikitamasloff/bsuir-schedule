package com.nikitamaslov.bsuirschedule.ui.main

data class FilterModel (
        var isScheduleTypeDefault: Boolean,
        var subgroup: Int,
        var subjects: List<String>?,
        var lessonType: List<String>?,
        var isCompleted: Boolean,
        var isColor: Boolean,
        var currentColor: Int?,
        var labels: List<Int>?
)