package com.nikitamaslov.bsuirschedule.data.local.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SharedPreferencesProvider(context: Context) {

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    }

    fun provide(): SharedPreferences = sharedPreferences

}