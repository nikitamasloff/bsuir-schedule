package com.nikitamaslov.bsuirschedule.data.local.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesProxy (context: Context) {

    private val provider: SharedPreferencesProvider by lazy { SharedPreferencesProvider(context) }
    private val prefs: SharedPreferences by lazy { provider.provide() }


    fun contains(key: String) = prefs.contains(key)

    fun remove(key: String) = prefs.edit { remove(key) }

    fun clear() = prefs.edit { clear() }

    /*
    * Get
    */

    fun getInt(key: String, defaultValue: Int): Int = prefs.getInt(key, defaultValue)

    fun getBoolean(key: String, defaultValue: Boolean): Boolean = prefs.getBoolean(key, defaultValue)

    fun getString(key: String, defaultValue: String?): String? = prefs.getString(key, defaultValue)

    @JvmName("getNonNullString")
    fun getString(key: String, defaultValue: String): String = prefs.getString(key, defaultValue) ?: defaultValue

    fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>? =
        prefs.getStringSet(key, defaultValue)

    @JvmName("getNonNullStringSet")
    fun getStringSet(key: String, defaultValue: Set<String>): Set<String> =
        prefs.getStringSet(key, defaultValue) ?: defaultValue

    /*
    * Set
    */

    fun putInt(key: String, value: Int) = prefs.edit { putInt(key, value) }

    fun putBoolean(key: String, value: Boolean) = prefs.edit { putBoolean(key, value) }

    fun putString(key: String, value: String?) = prefs.edit { putString(key, value) }

    fun putStringSet(key: String, value: Set<String>?) = prefs.edit { putStringSet(key, value) }

    operator fun set(key: String, value: Int) = putInt(key, value)

    operator fun set(key: String, value: Boolean) = putBoolean(key, value)

    operator fun set(key: String, value: String) = putString(key, value)

    operator fun set(key: String, value: Set<String>) = putStringSet(key, value)

    private inline fun SharedPreferences.edit(func: SharedPreferences.Editor.() -> Unit) {
        val editor = edit()
        editor.func()
        editor.apply()
    }

}