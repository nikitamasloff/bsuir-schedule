@file: [JvmName("Extensions") JvmMultifileClass]

package com.nikitamaslov.bsuirschedule.utils.extension

import android.support.annotation.StringRes
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.view.View

fun View.snackbar(text: String = "", length: Int = Snackbar.LENGTH_SHORT, init: Snackbar.() -> Unit = {} ){
    val snackbar = Snackbar.make(this,text,length)
    snackbar.init()
    snackbar.show()
}

fun Snackbar.text(text: String){
    this.setText(text)
}

fun Snackbar.text(@StringRes textRes: Int){
    this.setText(textRes)
}

fun Snackbar.duration(millis: Long){
    this.duration = millis.toInt()
}

fun Snackbar.duration(snackBarConst: Int){
    this.duration = snackBarConst
}

fun Snackbar.action(name: String, color: Int? = null, onClick: (View) -> Unit = {} ){
    this.setAction(name, onClick)
    color?.let { this.setActionTextColor(it) }
}

fun Snackbar.action(@StringRes name: Int, color: Int? = null, onClick: (View) -> Unit = {} ){
    this.setAction(name, onClick)
    color?.let { this.setActionTextColor(it) }
}

fun Snackbar.onDismiss(onDismiss: BaseTransientBottomBar.BaseCallback<Snackbar>.(event: Int) -> Unit = {}){
    this.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            onDismiss(event)
        }
    })
}