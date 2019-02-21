package com.nikitamaslov.bsuirschedule.ui.main

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nikitamaslov.bsuirschedule.R
import com.nikitamaslov.bsuirschedule.data.local.sharedpreferences.SharedPreferencesProxy
import com.nikitamaslov.bsuirschedule.data.model.Employee
import com.nikitamaslov.bsuirschedule.data.model.Group
import com.nikitamaslov.bsuirschedule.data.model.Host
import com.nikitamaslov.bsuirschedule.utils.MultitypeSerializer
import com.nikitamaslov.bsuirschedule.utils.Resources

class FilterPreferences (context: Context) {

    private val prefs = SharedPreferencesProxy(context)
    private val res = Resources(context)
    private val gson = Gson()

    val keyIsScheduleTypeDefault: String = res.string(R.string.pref_key_filter_is_schedule_type_default)
    val keySubgroup: String = res.string(R.string.pref_key_filter_subgroup)
    val keyLessonType: String = res.string(R.string.pref_key_filter_lesson_type)
    val keyLabels: String = res.string(R.string.pref_key_filter_labels)
    val keyIsCompleted: String = res.string(R.string.pref_key_filter_is_show_completed)
    val keyIsColor: String = res.string(R.string.pref_key_filter_is_color_current)
    private val keyCurrentColor: String = res.string(R.string.pref_key_filter_current_color)
    private val keySelectionValue = "selection_value_key"
    private val keySaved = "key_saved"

    var selection: Host?
        get() = MultitypeSerializer.deserialize(Host::class, prefs.getString(keySelectionValue, null))
        set(value) = prefs.putString(keySelectionValue, MultitypeSerializer.serialize(value))

    var isScheduleDefault: Boolean
        get() = prefs.getBoolean(keyIsScheduleTypeDefault, true)
        set(value) = prefs.putBoolean(keyIsScheduleTypeDefault, value)

    var subgroup: Int
        get() = prefs.getInt(keySubgroup,0)
        set(value) = prefs.putInt(keySubgroup,value)

    var lessonType: List<String>?
        get() {
            val data = prefs.getStringSet(keyLessonType, null) ?: return null
            val resArray = res.stringArray(R.array.lesson_type_values)
            return ArrayList(data.filter { resArray.contains(it) })
        }
        set(value) = prefs.putStringSet(keyLessonType, HashSet(value))

    val isCompleted: Boolean
        get() = prefs.getBoolean(keyIsCompleted, true)

    val isColor: Boolean
        get() = prefs.getBoolean(keyIsColor, false)

    val currentColor: Int?
        get() {
            val value = prefs.getInt(keyCurrentColor, -1)
            if (value == -1) return null
            return value
        }

    var labels: List<Int>?
        get() = gson.fromJson(prefs.getString(keyLabels, null), object : TypeToken<List<Int>?>(){}.type)
        set(value) = prefs.putString(keyLabels, gson.toJson(value, object : TypeToken<List<Int>?>(){}.type))

    fun key(data: Host?): String?
            = when (data) {
        is Group -> data.number
        is Employee -> data.fio
        else -> null
    }

    fun getSubjects(data: Host?): List<String>? =
        key(data)?.let { prefs.getStringSet(it, null)?.toList() }

    fun setSubjects(data: Host?, value: List<String>?) =
        key(data)?.let { prefs.putStringSet(it, value?.toSet()) }

    val form get() = FilterModel(
        lessonType = lessonType,
        isColor = isColor,
        isCompleted = isCompleted,
        subjects = getSubjects(selection),
        labels = labels,
        subgroup = subgroup,
        isScheduleTypeDefault = isScheduleDefault,
        currentColor = currentColor
    )

    val persisted = Persisted()

    inner class Persisted {

        private var objects: List<String>
            get() = prefs.getStringSet(keySaved,HashSet()).toList()
            set(value) = prefs.putStringSet(keySaved,HashSet(value))

        fun add(vararg items: Host?){
            objects += items.mapNotNull(MultitypeSerializer::serialize)
        }

        fun remove(vararg items: Host?){
            objects -= items.mapNotNull(MultitypeSerializer::serialize)
            if (items.contains(selection)) {
                selection = null
            }
        }

        fun getAll(): List<Host> = objects
            .mapNotNull { MultitypeSerializer.deserialize(Host::class, it) }

    }

}