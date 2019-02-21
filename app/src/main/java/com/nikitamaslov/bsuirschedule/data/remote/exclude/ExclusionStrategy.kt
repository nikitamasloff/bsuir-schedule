package com.nikitamaslov.bsuirschedule.data.remote.exclude

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

object ExclusionStrategy : ExclusionStrategy {
    override fun shouldSkipClass(clazz: Class<*>?): Boolean = false
    override fun shouldSkipField(f: FieldAttributes?): Boolean =
        f?.getAnnotation(Exclude::class.java) != null
}