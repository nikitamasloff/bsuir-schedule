@file: [JvmName("Extensions") JvmMultifileClass]

package com.nikitamaslov.bsuirschedule.utils.extension

import android.support.design.widget.Snackbar
import android.view.View
import com.nikitamaslov.bsuirschedule.R

fun View.notifyNoItemsSelected() = snackbar {
    text(R.string.notification_nothing_selected)
    duration(Snackbar.LENGTH_SHORT)
}

fun View.notifyAlreadyDownloaded(what: String) = snackbar {
    text(context.getString(R.string.search_notification_already_downloaded_template, what))
    duration(Snackbar.LENGTH_SHORT)
}

fun View.notifyNoInternetConnection() = snackbar {
    text(context.getString(R.string.notification_no_internet_connection))
    duration(Snackbar.LENGTH_SHORT)
}

fun View.notifyServerProblems() = snackbar {
    text(context.getString(R.string.notification_server_problems))
    duration(Snackbar.LENGTH_SHORT)
}