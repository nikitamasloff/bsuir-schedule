@file: [JvmName("Extensions") JvmMultifileClass]

package com.nikitamaslov.bsuirschedule.utils.extension

import android.content.Context
import android.support.annotation.StringRes
import com.yarolegovich.lovelydialog.LovelyStandardDialog

fun Context.showLovelyStandardDialog(dialogBuilder: LovelyStandardDialog.() -> Unit){

    val builder = LovelyStandardDialog(this)
    builder.dialogBuilder()
    val dialog = builder.create()
    dialog.show()

}

fun LovelyStandardDialog.positiveButton(@StringRes text: Int = android.R.string.ok, onClick: () -> Unit = {} ){
    this.setPositiveButton(text) { onClick()}
}

fun LovelyStandardDialog.negativeButton(@StringRes text: Int = android.R.string.cancel, onClick: () -> Unit = {} ){
    this.setNegativeButton(text) { onClick()}
}

fun LovelyStandardDialog.positiveButton(text: String?, onClick: () -> Unit = {} ){
    this.setPositiveButton(text) { onClick()}
}

fun LovelyStandardDialog.negativeButton(text: String?, onClick: () -> Unit = {} ){
    this.setNegativeButton(text) { onClick()}
}