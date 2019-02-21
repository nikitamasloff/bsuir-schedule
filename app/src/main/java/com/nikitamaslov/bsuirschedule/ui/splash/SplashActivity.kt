package com.nikitamaslov.bsuirschedule.ui.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.nikitamaslov.bsuirschedule.R
import com.nikitamaslov.bsuirschedule.data.local.database.Database
import com.nikitamaslov.bsuirschedule.data.local.sharedpreferences.SharedPreferencesProxy
import com.nikitamaslov.bsuirschedule.data.model.Auditory
import com.nikitamaslov.bsuirschedule.data.model.Employee
import com.nikitamaslov.bsuirschedule.data.model.Group
import com.nikitamaslov.bsuirschedule.data.remote.Api
import com.nikitamaslov.bsuirschedule.ui.main.MainActivity
import com.nikitamaslov.bsuirschedule.utils.HandlerThread
import com.nikitamaslov.bsuirschedule.utils.Resources
import com.nikitamaslov.bsuirschedule.utils.extension.connectedToInternet
import kotlinx.android.synthetic.main.activity_launch.*
import java.io.IOException

class SplashActivity : AppCompatActivity() {

    companion object {

        private const val KEY_LISTS_DOWNLOADED = "key_list_downloaded"
        private const val UPDATE_DELAY: Long = 1000

    }

    private lateinit var preferences: SharedPreferencesProxy
    private lateinit var res: Resources
    private lateinit var thread: HandlerThread
    private lateinit var api: Api
    private lateinit var database: Database.Proxy


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        init(this)

        when {
            preferences.getBoolean(KEY_LISTS_DOWNLOADED, false) -> launchMainActivity()
            connectedToInternet() -> downloadLists()
            else -> applyNoInternetConnectionState()
        }

    }

    override fun onDestroy() {
        destroy()
        super.onDestroy()
    }

    private fun init(context: Context) {
        preferences = SharedPreferencesProxy(context)
        res = Resources(context)
        api = Api()
        database = Database.proxy(context)
        thread = HandlerThread().also { it.start() }
    }

    private fun destroy() {
        thread.quit()
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun applyNoInternetConnectionState() {
        launch_progress_bar.visibility = View.GONE
        launch_title.text = res.string(R.string.no_internet_connection)
        launch_description.text = res.string(R.string.check_internet_connection)
        launch_retry_button.visibility = View.VISIBLE
        launch_retry_button.setOnClickListener {
            if (connectedToInternet()) downloadLists()
            else notifyNoInternetConnection(launch_title)
        }
    }

    private fun applyServerErrorState() {
        launch_progress_bar.visibility = View.GONE
        launch_title.text = res.string(R.string.unavailable_notification)
        launch_description.text = res.string(R.string.server_error_notification)
        launch_retry_button.visibility = View.VISIBLE
        launch_retry_button.setOnClickListener {
            launch_retry_button.setOnClickListener {
                if (connectedToInternet()) downloadLists()
                else notifyNoInternetConnection(launch_title)
            }
        }
    }

    private fun applyListsDownloadingState() {
        launch_title.text = res.string(R.string.loading_notification)
        launch_description.text = res.string(R.string.list_of_teachers_and_groups)
        launch_retry_button.visibility = View.GONE
        launch_progress_bar.visibility = View.VISIBLE
        //remove click listeners
        launch_retry_button.setOnClickListener {  }
    }

    private fun startConnectionAutoCheck(doOnConnection: () -> Unit) {
        thread.onUiDelayed(object : Runnable {
            override fun run() {
                if (connectedToInternet()) doOnConnection()
                else thread.onUiDelayed(this, UPDATE_DELAY)
            }
        }, UPDATE_DELAY)
    }

    private fun downloadLists() {
        applyListsDownloadingState()
        thread.onWorker {
            try {
                val groups: Array<Group> = api.query(Group::class)
                val employees: Array<Employee> = api.query(Employee::class)
                val auditory: Array<Auditory> = api.query(Auditory::class)

                database.insert(*groups)
                database.insert(*employees)
                database.insert(*auditory)

                thread.onUi {
                    markListsDownloaded()
                    launchMainActivity()
                }

            } catch (e: IOException) {
                e.printStackTrace()
                thread.onUi {
                    if (connectedToInternet()) {
                        applyServerErrorState()
                    } else {
                        applyNoInternetConnectionState()
                        startConnectionAutoCheck { downloadLists() }
                    }
                }
            }
        }
    }

    private fun markListsDownloaded() {
        preferences.putBoolean(KEY_LISTS_DOWNLOADED, true)
    }

    private fun notifyNoInternetConnection(view: View) {
        Snackbar.make(view, res.string(R.string.no_internet_connection), Snackbar.LENGTH_SHORT).show()
    }

}