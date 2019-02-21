package com.nikitamaslov.bsuirschedule.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.*
import android.support.v4.content.ContextCompat

class Resources (context: Context) {

    private val context = context.applicationContext

    fun string(@StringRes resId: Int): String =
        context.getString(resId)

    fun string(@StringRes resId: Int, vararg formatArgs: Any): String =
        context.resources.getString(resId, *formatArgs)

    fun stringArray(@ArrayRes resId: Int): Array<String> =
        context.resources.getStringArray(resId)

    fun intArray(@ArrayRes resId: Int): IntArray =
        context.resources.getIntArray(resId)

    @ColorInt
    fun color(@ColorRes resId: Int): Int =
        ContextCompat.getColor(context, resId)

    fun drawable(@DrawableRes resId: Int): Drawable? =
            ContextCompat.getDrawable(context, resId)

}