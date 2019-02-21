package com.nikitamaslov.bsuirschedule.ui.search

import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.WorkerThread
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.florent37.materialviewpager.header.MaterialViewPagerHeaderDecorator
import com.nikitamaslov.bsuirschedule.R
import com.nikitamaslov.bsuirschedule.data.local.database.Database
import com.nikitamaslov.bsuirschedule.data.model.*
import com.nikitamaslov.bsuirschedule.ui.base.BaseLazyFragment
import com.nikitamaslov.bsuirschedule.ui.base.BaseRecyclerView
import com.nikitamaslov.bsuirschedule.ui.main.FilterPreferences
import com.nikitamaslov.bsuirschedule.utils.HandlerThread
import com.nikitamaslov.bsuirschedule.utils.Resources
import com.nikitamaslov.bsuirschedule.utils.extension.negativeButton
import com.nikitamaslov.bsuirschedule.utils.extension.notifyNoItemsSelected
import com.nikitamaslov.bsuirschedule.utils.extension.positiveButton
import com.nikitamaslov.bsuirschedule.utils.extension.showLovelyStandardDialog

class SavedFragment: BaseLazyFragment() {

    companion object {
        fun instance() = SavedFragment()

        private const val TYPE_GROUP = 1
        private const val TYPE_TEACHER = 2

    }

    override val viewStubLayoutResId = R.layout.recycler_view

    private lateinit var savedList: List<Host>
    private lateinit var accumulator: ArrayList<Host>

    private lateinit var recyclerView: RecyclerView

    private var editModeOn: Boolean = false
        set(value) {
            field = value
            notifyEditModeChanged()
        }

    private var thread: HandlerThread? = null
    private lateinit var database: Database.Proxy
    private lateinit var res: Resources
    private lateinit var filterPrefs: FilterPreferences
    private lateinit var callback: Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callback = context as Callback
        init(requireContext())
    }

    override fun onCreateViewAfterViewStubInflated(inflatedView: View, savedInstanceState: Bundle?) {

        recyclerView = inflatedView.findViewById(R.id.single_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(MaterialViewPagerHeaderDecorator())
        recyclerView.adapter = RecyclerAdapter()

    }

    override fun onDestroy() {
        thread?.quit()
        super.onDestroy()
    }

    private fun init(context: Context) {
        thread = HandlerThread().also { it.start() }
        database = Database.proxy(context)
        res = Resources(context)
        filterPrefs = FilterPreferences(context)
        savedList = filterPrefs.persisted.getAll()
        accumulator = ArrayList()
    }


    @WorkerThread
    private fun removeEditListFromDB() {
        filterPrefs.persisted.remove(*accumulator.toTypedArray())
        accumulator.forEach {
            (it as? Group)?.run {
                if ((filterPrefs.selection as? Group)?.id == it.id) filterPrefs.selection = null
                it.id?.let { id -> database.deleteById(GroupSchedule::class, id) }
            }
            (it as? Employee)?.run {
                if ((filterPrefs.selection as? Employee)?.id == it.id) filterPrefs.selection = null
                it.id?.let { id -> database.deleteById(EmployeeSchedule::class, id) }
            }
        }
    }

    private fun showDeleteDialog() {
        if (accumulator.isEmpty()) {
            view?.notifyNoItemsSelected()
        } else {
            context?.showLovelyStandardDialog {
                setTitle(R.string.search_delete_dialog_title)
                setMessage(accumulator.asDialogDescription())
                positiveButton(R.string.search_delete_dialog_positive_button) {
                    thread?.onWorker {
                        filterPrefs.persisted.remove(*accumulator.toTypedArray())
                        removeEditListFromDB()
                        editModeOn = false
                        thread?.onUi { updateAdapter() }
                    }
                }
                negativeButton { this.dismiss() }
                setIcon(R.drawable.search_delete_trash_icon_black_20dp)
                    .apply { context?.let { setTopColor(ContextCompat.getColor(it, R.color.lovely_dialog_top_color)) } }
            }
        }
    }

    private fun updateAdapter(isGroupFirst: Boolean = true) {
        val persisted = filterPrefs.persisted.getAll()
        val sorted = if (isGroupFirst) {
            ArrayList(persisted.filterIsInstance<Group>() + persisted.filterIsInstance<Employee>())
        } else ArrayList(persisted.filterIsInstance<Employee>() + persisted.filterIsInstance<Group>())
        (recyclerView.adapter as RecyclerAdapter).updateItems(sorted)
    }

    private fun notifyEditModeChanged() {
        thread?.onUi {
            recyclerView.adapter?.notifyDataSetChanged()
            if (editModeOn) {
                callback.notifyEditOn()
            } else {
                accumulator = ArrayList()
                callback.notifyEditOff()
            }
        }
        accumulator = ArrayList()
    }

    /*
    * Callback
    */

    interface Callback {

        fun notifyEditOn()

        fun notifyEditOff()

    }

    fun onDeletePressed() {
        showDeleteDialog()
    }

    fun onCancelEdit(): Boolean {
        val temp = editModeOn
        editModeOn = false
        return temp
    }

    fun onUpdatePressed() {
        updateAdapter()
    }

    fun onPriorityPressed(isGroupFirst: Boolean) {
        updateAdapter(isGroupFirst)
    }


    private inner class RecyclerAdapter : BaseRecyclerView.Adapter<Host>(savedList) {

        @LayoutRes
        private val layoutResId = R.layout.item_search

        override fun getItemViewType(position: Int): Int = when (items[position]) {
            is Group -> TYPE_GROUP
            is Employee -> TYPE_TEACHER
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return viewHolder(LayoutInflater.from(activity).inflate(layoutResId, parent, false), viewType)
        }

        override fun layoutResId(position: Int, obj: Host): Int = layoutResId

        override fun viewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                TYPE_GROUP -> ViewHolder<Group>(view)
                TYPE_TEACHER -> ViewHolder<Employee>(view)
                else -> throw IllegalStateException("unknown layoutResId")
            }
        }

    }

    private inner class ViewHolder <T : Host> (itemView: View) : RecyclerView.ViewHolder(itemView), BaseRecyclerView.Binder<T>{

        private val layout: ViewGroup = itemView.findViewById(R.id.search_holder_layout)
        private val cardView: CardView = itemView.findViewById(R.id.search_holder_card_view)
        private val titleTextView: TextView = itemView.findViewById(R.id.search_holder_title)
        private val description: TextView = itemView.findViewById(R.id.search_holder_description)
        private val idTextView: TextView = itemView.findViewById(R.id.search_holder_id)
        private val selectionIndicator: View = itemView.findViewById(R.id.search_selection_indicator)

        private var data: T? = null

        init {
            layout.setBackgroundColor(layoutBackgroundColor())
            cardView.setCardBackgroundColor(cardBackgroundColor())
            selectionIndicator.visibility = View.VISIBLE

            itemView.setOnClickListener {
                if (editModeOn) {
                    toggle(data)
                }
                else {
                    filterPrefs.selection = data
                    finishActivityWithResult()
                }
            }

            itemView.setOnLongClickListener {
                if (!editModeOn) {
                    editModeOn = true
                }
                toggle(data)
                return@setOnLongClickListener true
            }

        }

        override fun bind(data: T) {
            this.data = data

            checkIsLastSelected()

            if (accumulator.contains(data)) select()
            else unselect()

            with(data) {
                when (this){
                    is Group -> {
                        titleTextView.text = number
                        description.text = course(res)
                        idTextView.text = id(res)
                    }
                    is Employee -> {
                        titleTextView.text = fullName(res)
                        description.text = department(res)
                        idTextView.text = id(res)
                    }
                }
            }
        }

        private fun layoutBackgroundColor(): Int {
            return ContextCompat.getColor(itemView.context, R.color.search_holder_background)
        }

        private fun cardBackgroundColor(): Int {
            return ContextCompat.getColor(itemView.context, R.color.search_holder_card_default)
        }

        private fun select(){
            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.search_holder_card_selected))
        }

        private fun unselect(){
            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.search_holder_card_default))
        }

        private fun checkIsLastSelected(){
            if (isLastSelected(data)){
                selectionIndicator.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.search_holder_selected_indicator))
            }else selectionIndicator.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.search_holder_card_default))
        }

        private fun toggle(data: T?) = data?.let {
            if (accumulator.contains(data)){
                accumulator.remove(data)
                unselect()
            }else {
                accumulator.add(data)
                select()
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }

        private fun finishActivityWithResult() {
            this@SavedFragment.activity?.apply {
                setResult(SearchActivity.RESULT_CHANGED)
                finish()
            }
        }

        private fun isLastSelected(data: T?): Boolean = filterPrefs.selection.let {
            when {
                it is Group && data is Group -> data.id == it.id
                it is Employee && data is Employee -> data.id == it.id
                else -> false
            }
        }

    }

    /*
    * Util function
    * */

    private fun ArrayList<Host>.asDialogDescription(): String {
        val string = when {
            this.size == 0 -> return ""
            this[0] is Employee -> (this[0] as Employee).fio
            this[0] is Group -> (this[0] as Group).number
            else -> return ""
        }
        val sb = StringBuilder(string ?: "")
        for (index in 1 until this.size) {
            this[index].let { item ->
                (item as? Group)?.let { sb.append(", ${it.number}") }
                (item as? Employee)?.let { sb.append(", ${it.fio}") }
            }
        }
        return sb.toString()
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