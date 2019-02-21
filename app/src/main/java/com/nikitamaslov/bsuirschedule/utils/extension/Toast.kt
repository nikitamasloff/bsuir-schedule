@file: [JvmName("Extensions") JvmMultifileClass]

package com.nikitamaslov.bsuirschedule.utils.extension

import android.content.Context
import android.widget.Toast

fun Context.toast(text: String?, duration: Int = Toast.LENGTH_SHORT, block: Toast.() -> Unit = {}){
    val toast: Toast = Toast.makeText(this, text, duration)
    toast.block()
    toast.show()
}