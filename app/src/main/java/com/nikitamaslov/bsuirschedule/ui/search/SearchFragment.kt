package com.nikitamaslov.bsuirschedule.ui.search

import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import com.github.florent37.materialviewpager.header.MaterialViewPagerHeaderDecorator
import com.nikitamaslov.bsuirschedule.R
import com.nikitamaslov.bsuirschedule.data.local.database.Database
import com.nikitamaslov.bsuirschedule.data.model.*
import com.nikitamaslov.bsuirschedule.data.remote.Api
import com.nikitamaslov.bsuirschedule.ui.base.BaseLazyFragment
import com.nikitamaslov.bsuirschedule.ui.base.BaseRecyclerView
import com.nikitamaslov.bsuirschedule.ui.main.FilterPreferences
import com.nikitamaslov.bsuirschedule.utils.HandlerThread
import com.nikitamaslov.bsuirschedule.utils.Resources
import com.nikitamaslov.bsuirschedule.utils.extension.*
import com.yarolegovich.lovelydialog.LovelyStandardDialog
import kotlin.reflect.KClass

class SearchFragment<T : Host>: BaseLazyFragment(), SearchView.OnQueryTextListener {

    companion object {

        private const val KEY_TYPE = "type"
        private const val TYPE_GROUP = 1
        private const val TYPE_EMPLOYEE = 2

        inline fun <reified T : Host> instance(): SearchFragment<T> = instance(T::class)

        fun <T : Host> instance(kClass: KClass<T>): SearchFragment<T> {
            val fragment = SearchFragment<T>()
            val type = when (kClass) {
                Group::class -> TYPE_GROUP
                Employee::class -> TYPE_EMPLOYEE
                else -> throw IllegalArgumentException("invalid host kClass")
            }
            val args = Bundle()
            args.putInt(KEY_TYPE, type)
            fragment.arguments = args
            return fragment
        }

    }

    override val viewStubLayoutResId = R.layout.recycler_view

    private var dataList: List<T> = emptyList()
    private var accumulator: List<T> = emptyList()

    private var recyclerView: RecyclerView? = null
    private var adapter: RecyclerAdapter? = null

    private var thread: HandlerThread? = null
    private lateinit var database: Database.Proxy
    private lateinit var api: Api
    private lateinit var res: Resources
    private lateinit var filterPrefs: FilterPreferences
    private lateinit var callback: Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callback = context as Callback
        init(requireContext())
    }

    override fun onCreateViewAfterViewStubInflated(inflatedView: View, savedInstanceState: Bundle?) {

        recyclerView = inflatedView.findViewById<RecyclerView>(R.id.single_recycler_view).apply {
            layoutManager = LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL,
                false)
            addItemDecoration(MaterialViewPagerHeaderDecorator())
        }

        initAdapter()

    }

    override fun onDestroy() {
        thread?.quit()
        super.onDestroy()
    }

    private fun init(context: Context) {
        thread = HandlerThread().also { it.start() }
        database = Database.proxy(context)
        api = Api()
        res = Resources(context)
        filterPrefs = FilterPreferences(context)
    }

    private fun initAdapter() {
        thread?.onWorker {
            dataList = dataList()
            accumulator = dataList
            thread?.onUi {
                adapter = RecyclerAdapter(dataList)
                recyclerView?.adapter = adapter
            }
        }
    }

    private fun suit(data: T, filter: String?): Boolean {
        data.run {
            if (filter == null || filter.isEmpty()) return true
            return when (this) {
                is Group -> number?.startsWith(filter, true) ?: false
                is Employee -> fullName(res).startsWith(filter, true)
                        || department(res).startsWith(filter, true)
                else -> false
            }
        }
    }


    /*
    * Callback
    */

    interface Callback {

        fun onNewScheduleAdded()

    }

    override fun onQueryTextSubmit(p0: String?): Boolean = false

    override fun onQueryTextChange(filter: String?): Boolean {
        accumulator = dataList.filter { suit(it, filter) }
        accumulator.run { adapter?.updateItems(this) }
        return true
    }

    private inner class RecyclerAdapter(list: List<T>) : BaseRecyclerView.Adapter<T>(list) {

        @LayoutRes
        private val layoutResId = R.layout.item_search

        override fun layoutResId(position: Int, obj: T): Int = layoutResId

        override fun viewHolder(view: View, viewType: Int): RecyclerView.ViewHolder =
            SearchableViewHolder(view)

    }


    private var objToLoad: T? = null

    private inner class SearchableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BaseRecyclerView.Binder<T> {

        private val titleTextView: TextView = itemView.findViewById(R.id.search_holder_title)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.search_holder_description)
        private val idTextView: TextView = itemView.findViewById(R.id.search_holder_id)

        private var data: T? = null

        override fun bind(data: T) {
            this.data = data
            this.data?.run {
                when (this) {
                    is Group -> {
                        titleTextView.text = number
                        descriptionTextView.text = course(res)
                        idTextView.text = id(res)
                    }
                    is Employee -> {
                        titleTextView.text = fullName(res)
                        descriptionTextView.text = department(res)
                        idTextView.text = id(res)
                    }
                }
            }
        }

        private fun isAlreadyDownloaded(): Boolean = data?.run {
            when (this) {
                is Group -> id?.let { database.queryById(GroupSchedule::class, it).isNotEmpty() } ?: false
                is Employee -> id?.let { database.queryById(EmployeeSchedule::class, it).isNotEmpty() } ?: false
                else -> throw IllegalStateException("unknown host type")
            }
        } ?: true

        private fun loadToDB(obj: T?) = obj?.run {
                    when (this) {
                        is Group -> {
                            val array = api.query(GroupSchedule::class, id ?: return@run)
                            database.insert(*array)
                        }
                        is Employee -> {
                            val array = api.query(EmployeeSchedule::class, id ?: return@run)
                            database.insert(*array)
                        }
                    }
                }

        private fun dialogName(): String =
                data?.run {
                    when (this) {
                        is Group -> number
                        is Employee -> fio
                        else -> ""
                    }
                } ?: ""

        init {
            itemView.setOnClickListener {
                thread?.onWorker {
                    if (!isAlreadyDownloaded()) {
                        if (!itemView.context.connectedToInternet()) {
                            itemView.notifyNoInternetConnection()
                            return@onWorker
                        }
                        thread?.onUi {
                            objToLoad = data
                            showAddDialog(context, dialogName()) {
                                thread?.onWorker {
                                    try {
                                        loadToDB(objToLoad)
                                        filterPrefs.persisted.add(objToLoad)
                                    } catch (e: Exception) {
                                        thread?.onUi {
                                            this.dismiss()
                                            notifyErrorWhileDownloading(itemView)
                                        }
                                    }
                                    thread?.onUi {
                                        callback.onNewScheduleAdded()
                                        this.dismiss()
                                    }
                                }
                            }
                        }
                    } else {
                        thread?.onUi {
                            view?.notifyAlreadyDownloaded(dialogName())
                        }
                    }
                }
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    private fun dataList(): List<T> {
        return when (arguments?.getInt(KEY_TYPE)) {
            TYPE_GROUP -> database.query(Group::class).sortedBy(Group::number)
            TYPE_EMPLOYEE -> database.query(Employee::class).sortedBy(Employee::fio)
            else -> throw IllegalStateException("invalid type in fragment arguments")
        } as List<T>
    }

    private fun notifyErrorWhileDownloading(view: View) = with(view) {
        if (!context.connectedToInternet()) notifyNoInternetConnection()
        else notifyServerProblems()
    }

    private fun showAddDialog(context: Context?, message: String?, loadToDB: LovelyStandardDialog.() -> Unit){
        context?.showLovelyStandardDialog {
            setTitle(R.string.search_add_dialog_title)
            setMessage(message)
            setTopColorRes(R.color.lovely_dialog_top_color)
            setIcon(ContextCompat.getDrawable(context,R.drawable.search_add_dialog_black_16dp))
            negativeButton(R.string.search_add_dialog_negative_button){ dismiss() }
            positiveButton (R.string.search_add_dialog_positive_button)
            setOnButtonClickListener(false) {view ->
                when(view?.id){
                    LovelyStandardDialog.NEGATIVE_BUTTON -> dismiss()
                    LovelyStandardDialog.POSITIVE_BUTTON -> {
                        setCancelable(false)
                        configureTitleView { title: TextView? ->
                            val anim = AlphaAnimation(0.1f, 1.0f)
                            anim.duration = 400
                            anim.startOffset = 20
                            anim.repeatMode = Animation.REVERSE
                            anim.repeatCount = Animation.INFINITE
                            title?.startAnimation(anim)
                        }
                        setOnButtonClickListener(false) { innerView ->
                            when (innerView.id) {
                                LovelyStandardDialog.NEGATIVE_BUTTON -> { }
                                LovelyStandardDialog.POSITIVE_BUTTON -> { }
                            }
                        }
                        setTitle(getString(R.string.search_add_dialog_loading))
                        loadToDB()
                    }
                }
            }
        }
    }

    private fun Group.id(res: Resources): String = res.string(R.string.id_template, id.toString())

    private fun Group.course(res: Resources): String =
        res.string(R.string.course_template, course.toString())

    private fun Employee.id(res: Resources): String = res.string(R.string.id_template, id.toString())

    private fun Employee.fullName(res: Resources): String = res.string(
        R.string.full_name_template,
        surname ?: "",
        name ?: "",
        patronymic ?: ""
    )

    private fun Employee.department(res: Resources): String =
        department.let {
            res.string(
                R.string.department_template, when {
                    it.isEmpty() -> ""
                    it.size == 1 -> it.first()
                    else -> it.joinToString(separator = ", ")
                }
            )
        }

}