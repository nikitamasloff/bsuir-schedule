package com.nikitamaslov.bsuirschedule.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.CardView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nikitamaslov.bsuirschedule.R
import com.nikitamaslov.bsuirschedule.data.local.database.Database
import com.nikitamaslov.bsuirschedule.data.model.*
import com.nikitamaslov.bsuirschedule.ui.base.BaseLazyFragment
import com.nikitamaslov.bsuirschedule.ui.base.BaseRecyclerView
import com.nikitamaslov.bsuirschedule.ui.edit.EditActivity
import com.nikitamaslov.bsuirschedule.ui.palette.Palette
import com.nikitamaslov.bsuirschedule.utils.HandlerThread
import com.nikitamaslov.bsuirschedule.utils.MultitypeSerializer
import com.nikitamaslov.bsuirschedule.utils.Resources
import com.nikitamaslov.bsuirschedule.utils.extension.negativeButton
import com.nikitamaslov.bsuirschedule.utils.extension.notifyNoItemsSelected
import com.nikitamaslov.bsuirschedule.utils.extension.positiveButton
import com.nikitamaslov.bsuirschedule.utils.extension.showLovelyStandardDialog
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.stone.vega.library.VegaLayoutManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.KClass

class ScheduleFragment<T : Schedule>: BaseLazyFragment() {

    companion object {

        private const val DAILY_MODE_PAGES_AMOUNT = 31
        private const val ENTIRE_MODE_PAGES_AMOUNT = 7

        private const val KEY_ID = "id"

        private const val KEY_HOST_TYPE = "host_type"
        private const val HOST_TYPE_GROUP_SCHEDULE = 1
        private const val HOST_TYPE_EMPLOYEE_SCHEDULE = 2
        private const val HOST_TYPE_EMPTY = 3

        inline fun <reified T : Schedule> instance(id: Int?): ScheduleFragment<T> =
            instance(T::class, id)

        fun <T : Schedule> instance(kClass: KClass<T>, id: Int?): ScheduleFragment<T> {
            val fragment = ScheduleFragment<T>()
            val type = when (kClass) {
                GroupSchedule::class -> HOST_TYPE_GROUP_SCHEDULE
                EmployeeSchedule::class -> HOST_TYPE_EMPLOYEE_SCHEDULE
                Schedule::class -> HOST_TYPE_EMPTY
                else -> throw IllegalArgumentException("invalid host kClass")
            }
            val args = Bundle()
            args.putInt(KEY_HOST_TYPE, type)
            id?.let { args.putInt(KEY_ID, it) }
            fragment.arguments = args
            return fragment
        }

    }

    private var dataList: List<T> = ArrayList()
    private var accumulator = ArrayList<T>()

    private val pagesAmount get() =
        if (filterPrefs.isScheduleDefault) DAILY_MODE_PAGES_AMOUNT
        else ENTIRE_MODE_PAGES_AMOUNT

    private var editEnabled: Boolean by Delegates.observable(false) { _, _, _ ->
        accumulator = ArrayList()
        val length = viewPager.adapter?.count ?: 0
        (0..length).forEach(::updatePage)
    }

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: SmartTabLayout

    private var thread: HandlerThread? = null
    private lateinit var res: Resources
    private lateinit var palette: Palette
    private lateinit var database: Database.Proxy
    private lateinit var filterPrefs: FilterPreferences
    private lateinit var callback: Callback

    override val viewStubLayoutResId = R.layout.layout_schedule

    override fun onCreateViewAfterViewStubInflated(inflatedView: View, savedInstanceState: Bundle?) {

        callback = context as Callback

        viewPager = inflatedView.findViewById(R.id.schedule_view_pager)
        tabLayout = inflatedView.findViewById(R.id.schedule_tab_layout)

        init(requireContext())
        initAdapter()

    }

    override fun onDestroy() {
        thread?.quit()
        super.onDestroy()
    }

    private fun init(context: Context) {
        thread = HandlerThread().also { it.start() }
        res = Resources(context)
        palette = Palette(res)
        database = Database.proxy(context)
        filterPrefs = FilterPreferences(context)
    }

    private fun initAdapter() {
        viewPager.offscreenPageLimit = pagesAmount
        thread?.onWorker {
            dataList = dataList()
            thread?.onUi {
                viewPager.adapter = ViewPagerAdapter()
                tabLayout.setViewPager(viewPager)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun dataList(): List<T> = arguments?.getInt(KEY_ID)?.let {
        when (arguments?.getInt(KEY_HOST_TYPE)) {
            HOST_TYPE_GROUP_SCHEDULE -> database.queryById(GroupSchedule::class, it).toList()
            HOST_TYPE_EMPLOYEE_SCHEDULE -> database.queryById(EmployeeSchedule::class, it).toList()
            HOST_TYPE_EMPTY -> emptyList()
            else -> throw IllegalStateException("invalid type in fragment arguments")
        } as List<T>
    } ?: emptyList()

    private fun updatePage(position: Int) {
        val recyclerView: RecyclerView? = viewPager.findViewWithTag(getTag(position))
        recyclerView?.adapter?.notifyDataSetChanged()
    }

    private fun updateViewPager() {
        initAdapter()
    }

    private fun removeEditListFromDB() {
        val teacherScheduleList = ArrayList<EmployeeSchedule>()
        val groupScheduleList = ArrayList<GroupSchedule>()
        accumulator.forEach {
            if (it is EmployeeSchedule) teacherScheduleList.add(it)
            if (it is GroupSchedule) groupScheduleList.add(it)
        }

        if (groupScheduleList.isNotEmpty()) {
            thread?.onWorker {
                database.delete(*groupScheduleList.toTypedArray())
            }
        }
        if (teacherScheduleList.isNotEmpty()) {
            thread?.onWorker {
                database.delete(*teacherScheduleList.toTypedArray())
            }
        }
        accumulator = ArrayList()
    }

    private fun launchEditActivityEditMode() {
        Intent(activity, EditActivity::class.java).apply {
            putExtra(
                EditActivity.EXTRA_DATA_OBJECT,
                MultitypeSerializer.serialize(accumulator.getOrNull(0)))
            val key = accumulator.getOrElse(0) { return@apply }
            putExtra(
                EditActivity.EXTRA_DATA_KEY,
                when (key) {
                    is GroupSchedule -> MultitypeSerializer.serialize(key.group)
                    is EmployeeSchedule -> MultitypeSerializer.serialize(key.employee)
                    else -> return@apply
                })
            activity?.startActivityForResult(this, MainActivity.REQUEST_CODE_EDIT_ACTIVITY)
        }
    }

    private fun launchEditActivityAddMode() {
        Intent(activity, EditActivity::class.java).apply {
            val key = filterPrefs.selection
            putExtra(
                EditActivity.EXTRA_DATA_KEY,
                when (key) {
                    is Group -> MultitypeSerializer.serialize(key)
                    is Employee -> MultitypeSerializer.serialize(key)
                    else -> return@apply
                })
            activity?.startActivityForResult(this, MainActivity.REQUEST_CODE_EDIT_ACTIVITY)
        }
    }

    private fun getTag(position: Int) = position

    private fun showDeleteDialog() {
        if (accumulator.isEmpty()) {
            view?.notifyNoItemsSelected()
        } else {
            context?.showLovelyStandardDialog {
                setTitle(R.string.search_delete_dialog_title)
                setMessage(accumulator.asDialogDescription())
                positiveButton(R.string.search_delete_dialog_positive_button) {
                    thread?.onWorker {
                        removeEditListFromDB()
                        thread?.onUi {
                            updateViewPager()
                            callback.notifyDeleteOff()
                            editEnabled = false
                        }
                    }
                }
                negativeButton { this.dismiss() }
                setIcon(R.drawable.search_delete_trash_icon_black_20dp)
                    .apply { context?.let { setTopColor(ContextCompat.getColor(it, R.color.lovely_dialog_top_color)) } }
            }
        }
    }

    /*
    * Callback
    */

    interface Callback {

        fun notifyDeleteEnabled()

        fun notifyDeleteOff()

        fun notifyEditEnabled()

        fun notifyEditOff()

    }

    fun onBackPressed(): Boolean {
        val cur = !editEnabled && viewPager.currentItem != 0
        if (cur) {
            viewPager.setCurrentItem(0, true)
        }
        val temp = editEnabled || cur
        if (editEnabled) {
            editEnabled = false
            callback.notifyDeleteOff()
            callback.notifyEditOff()
        }
        return temp
    }

    fun onCancelDelete() {
        editEnabled = false
    }

    fun onEditPressed() {
        launchEditActivityEditMode()
        callback.notifyDeleteOff()
    }

    fun onAddPressed() {
        launchEditActivityAddMode()
        editEnabled = false
        callback.notifyDeleteOff()
    }

    fun onDeletePressed() {
        showDeleteDialog()
    }


    private inner class ViewPagerAdapter : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val recyclerView = LayoutInflater.from(container.context)
                    .inflate(R.layout.recycler_view, container, false) as RecyclerView

            recyclerView.layoutManager = VegaLayoutManager()
            recyclerView.adapter = RecyclerViewAdapter(position)
            recyclerView.itemAnimator = DefaultItemAnimator()

            recyclerView.tag = getTag(position)

            container.addView(recyclerView)
            return recyclerView
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as? View)
        }

        override fun getItemPosition(obj: Any): Int = POSITION_NONE

        override fun getPageTitle(position: Int): CharSequence? =
                if (filterPrefs.isScheduleDefault) dayCalendarTitles[position]
                else weekCalendarTitles[position]

        override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

        override fun getCount(): Int = pagesAmount

    }

    private inner class RecyclerViewAdapter(position: Int) : BaseRecyclerView.Adapter<T>(dataList
            .filter { item -> suit(position, item) }
            .sortedWith(compareBy {
                when (it) {
                    is GroupSchedule -> it.startTime
                    is EmployeeSchedule -> it.startTime
                    else -> it.hashCode()
                }
            }).sortedWith(compareBy {
                when (it) {
                    is GroupSchedule -> it.endTime
                    is EmployeeSchedule -> it.endTime
                    else -> it.hashCode()
                }
            })
    ) {

        override fun layoutResId(position: Int, obj: T): Int =
                when {
                    filterPrefs.isScheduleDefault -> R.layout.item_schedule_simple
                    else -> R.layout.item_schedule
                }

        override fun viewHolder(view: View, viewType: Int): RecyclerView.ViewHolder =
                when (viewType) {
                    R.layout.item_schedule -> FullViewHolder(view)
                    else -> SimpleViewHolder(view)
                }


        private abstract inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BaseRecyclerView.Binder<T> {

            private val layout: ViewGroup by lazy { itemView.findViewById<ViewGroup>(R.id.schedule_holder_layout) }
            private val innerLayout: ViewGroup by lazy { itemView.findViewById<ViewGroup>(R.id.schedule_holder_inner_layout) }
            private val cardView: CardView by lazy { itemView.findViewById<CardView>(R.id.schedule_holder_card_view) }
            private val subject: TextView by lazy { itemView.findViewById<TextView>(R.id.schedule_holder_subject) }
            private val auditory: TextView by lazy { itemView.findViewById<TextView>(R.id.schedule_holder_auditory) }
            private val lessonType: TextView by lazy { itemView.findViewById<TextView>(R.id.schedule_holder_lesson_type) }
            private val time: TextView by lazy { itemView.findViewById<TextView>(R.id.schedule_holder_time) }
            private val people: TextView by lazy { itemView.findViewById<TextView>(R.id.schedule_holder_people) }
            private val note: TextView by lazy { itemView.findViewById<TextView>(R.id.schedule_holder_note) }
            protected val subgroup: TextView by lazy { itemView.findViewById<TextView>(R.id.schedule_holder_num_subgroup) }
            private val label: View = itemView.findViewById(R.id.schedule_holder_label)

            protected var data: T? = null

            override fun bind(data: T) {

                this.data = data

                when (data) {
                    is GroupSchedule -> {
                        data.color?.let { color -> if (color != 0) label.setBackgroundColor(color) }
                        data.subject?.let { subject.text = it }
                        data.lessonType.let { lessonType.text = it }
                        lessonType.setTextColor(color(data.lessonType))
                        auditory.text = auditory(data.auditory)
                        time.text = getString(R.string.schedule_time_template, data.startTime, data.endTime)
                        data.employee?.fio?.let { if (it.isNotEmpty()) people.text = it }
                        data.note?.let { if (it.isNotEmpty()) note.text = it }
                        if (needToColor(data)) {
                            filterPrefs.currentColor?.let { color -> innerLayout.setBackgroundColor(color) }
                        }

                    }
                    is EmployeeSchedule -> {
                        data.color?.let { color -> if (color != 0) label.setBackgroundColor(color) }
                        data.subject?.let { subject.text = it }
                        data.lessonType.let { lessonType.text = it }
                        lessonType.setTextColor(color(data.lessonType))
                        auditory.text = auditory(data.auditory)
                        time.text = getString(R.string.schedule_time_template, data.startTime, data.endTime)
                        data.group.let { if (it.isNotEmpty()) people.text = studentGroup(it) }
                        data.note?.let { if (it.isNotEmpty()) note.text = it }
                        if (needToColor(data)) {
                            filterPrefs.currentColor?.let { color -> innerLayout.setBackgroundColor(color) }
                        }

                    }
                }

                if (accumulator.contains(data)) {
                    select()
                } else {
                    unselect()
                }

                extraBind(data)

            }

            protected open fun extraBind(data: T) {}

            private fun toggle() {
                data?.run {
                    if (accumulator.contains(this)) {
                        unselect()
                        accumulator.remove(this)
                    } else {
                        select()
                        accumulator.add(this)
                    }
                }
                updatePage(viewPager.currentItem)
                if (accumulator.size == 1)
                    callback.notifyEditEnabled()
                else
                    callback.notifyEditOff()
            }

            private fun select() {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.schedule_holder_card_selected))
            }

            private fun unselect() {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.schedule_holder_card_default))
            }

            init {
                itemView.setOnLongClickListener {
                    if (!editEnabled) {
                        editEnabled = true
                        callback.notifyDeleteEnabled()
                    }
                    toggle()
                    true
                }
                itemView.setOnClickListener {
                    if (editEnabled) {
                        toggle()
                    }
                }
            }

        }

        inner class SimpleViewHolder(itemView: View) : ViewHolder(itemView) {

            override fun extraBind(data: T) {
                when (data) {
                    is GroupSchedule -> {
                        data.subgroup.let { if (it != 0) subgroup.text = it.toString() }
                    }
                    is EmployeeSchedule -> {
                        data.subgroup.let { if (it != 0) subgroup.text = it.toString() }
                    }
                }
            }

        }

        inner class FullViewHolder(itemView: View) : ViewHolder(itemView) {
            private val weekNumber = itemView.findViewById<TextView>(R.id.schedule_holder_week_number)
            override fun extraBind(data: T) {
                when (data) {
                    is GroupSchedule -> {
                        weekNumber.text = weekNumber(data.numberOfWeek)
                        data.subgroup.let { if (it != 0) subgroup.text = subgroup(it) }
                    }
                    is EmployeeSchedule -> {
                        weekNumber.text = weekNumber(data.numberOfWeek)
                        data.subgroup.let { if (it != 0) subgroup.text = subgroup(it) }
                    }
                }
            }


        }


    }

    /*
    * Schedule utils
    */

    private fun suit (position: Int, item: Schedule): Boolean{

        val isSuitCalendar: Boolean =
                if (filterPrefs.isScheduleDefault) isSuitDayCalendar(position, item)
                else isSuitWeekCalendar(position, item)

        if (!isSuitCalendar) {
            return false
        }

        val isSuitsFilter: Boolean =
                filterPrefs.run {
                    when (item) {
                        is GroupSchedule -> {
                            ((subgroup == 0 || item.subgroup == 0 || subgroup == item.subgroup)
                                    && (lessonType == null || lessonType?.contains(item.lessonType) == true)
                                    && (getSubjects(item.group) == null || getSubjects(item.group)?.contains(item.subject) == true)
                                    && (isCompleted || (!isScheduleDefault || !isSuitDayCalendar(0, item) || time(item.startTime, item.endTime) ?: 1 < 1)))
                                    && (item.color == null || labels?.contains(item.color!!) == true)
                        }
                        is EmployeeSchedule -> {
                            ((subgroup == 0 || item.subgroup == 0 || subgroup == item.subgroup)
                                    && lessonType == null || lessonType?.contains(item.lessonType) == true)
                                    && (getSubjects(item.employee) == null || getSubjects(item.employee)?.contains(item.subject) == true)
                                    && (isCompleted || (!isScheduleDefault || !isSuitDayCalendar(0, item) || time(item.startTime, item.endTime) ?: 1 < 1))
                                    && (item.color == null || labels?.contains(item.color!!) == true)
                        }
                    }
                }

        return isSuitCalendar && isSuitsFilter
    }

    private fun ArrayList<T>.asDialogDescription(): String {
        val map = map {
            when (it) {
                is GroupSchedule -> it.toDialogInfo()
                is EmployeeSchedule -> it.toDialogInfo()
                else -> ""
            }
        }
        val separator = " ;\n"
        return map.joinToString(separator)
    }

    private fun weekNumber(list: List<Int>?): String {
        if (list?.contains(0) != false) return ""
        if (list.size == 1) return res.string(R.string.week_template,list[0].toString())
        return res.string(R.string.weeks_template,list.toString().removePrefix("[").removeSuffix("]"))
    }

    private fun auditory(list: List<String>?): String {
        return list?.toString()?.removePrefix("[")?.removeSuffix("]") ?: ""
    }

    private fun studentGroup(list: List<String>?): String {
        return List(list?.size ?: return ""){ index ->
            list[index].takeIf { item ->
                list.count { countItem -> countItem.dropLast(1) == item.dropLast(1) } < 2 }
                    ?: "${list[index].dropLast(1)}x"
        }.distinct().sortedBy { it }.sortedWith(compareBy {!it.contains("x")})
                .toString().removePrefix("[").removeSuffix("]")
    }

    private fun subgroup(number: Int?): String =
            if (number == null || number == 0) ""
            else res.string(R.string.subgroup_template,number)

    private fun time(start: String?, end: String?): Int?{
        return try {
            val startTime = start?.replace(":","",true)?.toInt() ?: return null
            val endTime = end?.replace(":","",true)?.toInt() ?: return null
            val cur = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()).replace(":","",true).toInt()
            when {
                cur < startTime -> -1
                cur in startTime..endTime -> 0
                cur > endTime -> 1
                else -> null
            }
        }catch (e: Exception){
            null
        }
    }

    private fun color(type: String?): Int {
        val array = res.stringArray(R.array.lesson_type_values)
        val transparent = Color.parseColor("#00ffffff")
        if (type == null || type.isEmpty() || !array.contains(type))
            return transparent
        return when (type){
            array[0] -> res.color(R.color.schedule_type_lecture)
            array[1] -> res.color(R.color.schedule_type_practice)
            array[2] -> res.color(R.color.schedule_type_laboratory)
            else -> transparent
        }
    }

    private fun <T> needToColor(item: T): Boolean{
        return filterPrefs.isColor && filterPrefs.isScheduleDefault && when (item){
            is GroupSchedule ->
                isSuitDayCalendar(0,item) && time(item.startTime,item.endTime) == 0
            is EmployeeSchedule ->
                isSuitDayCalendar(0,item) && time(item.startTime,item.endTime) == 0
            else -> false
        }
    }

    private fun <T> isSuitWeekCalendar(position: Int, item: T): Boolean {
        return weekCalendarTitles[position] ==
                when (item){
                    is GroupSchedule -> item.dayOfWeek
                    is EmployeeSchedule -> item.dayOfWeek
                    else -> return false
                }
    }

    private fun isSuitDayCalendar(position: Int, item: Schedule): Boolean {
        val (dayOfWeek, numberOfWeek) = dayCalendarItems[position]
        val dayOfWeekEquality = when (item) {
            is GroupSchedule -> item.dayOfWeek == dayOfWeek
            is EmployeeSchedule -> item.dayOfWeek == dayOfWeek
        }
        val numberOfWeekEquality = when (item) {
            is GroupSchedule -> item.numberOfWeek.contains(0) || item.numberOfWeek.contains(numberOfWeek)
            is EmployeeSchedule -> item.numberOfWeek.contains(0) || item.numberOfWeek.contains(numberOfWeek)
        }
        return dayOfWeekEquality && numberOfWeekEquality

    }

    private fun GroupSchedule.toDialogInfo(): String =
            "$subject ($lessonType) [$startTime-$endTime]"

    private fun EmployeeSchedule.toDialogInfo(): String =
            "$subject ($lessonType) [$startTime-$endTime]"

    /*
    * Calendar utils
    */

    private val dayCalendarTitles: List<String> by lazy { dayCalendarTitles() }

    private fun dayCalendarTitles(): List<String> {
        //val engFull = res.stringArray(R.array.calendar_day_of_week_full_eng)
        //val rusShort = res.stringArray(R.array.calendar_day_of_week_short_rus)
        val calendarList = ArrayList<String>()
        val calendar = Calendar.getInstance()
        for (index in 0 until DAILY_MODE_PAGES_AMOUNT) {
            val string = StringBuilder(enDayOfWeekToShortRus(SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)))
            string.append(SimpleDateFormat(", dd ", Locale.getDefault()).format(calendar.time))
            string.append(enMonthToShortRus(SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)))
            string.append(", ${res.string(R.string.week_short_template,
                    calculateWeekNumber(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)).toString())} ")
            calendarList.add(index, string.toString())
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)
        }
        return calendarList
    }

    private val weekCalendarTitles: List<String> by lazy { res.stringArray(R.array.calendar_days_of_week_full_rus).toList() }

    private val dayCalendarItems: List<Pair<String,Int>> by lazy { dayCalendar() }

    private fun dayCalendar(): List<Pair<String, Int>> {
        val calendarList = ArrayList<Pair<String,Int>>()
        val calendar = Calendar.getInstance()
        for (index in 0 until DAILY_MODE_PAGES_AMOUNT){
            val dayOfWeek = enDayOfWeekToFullRus(SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time))
            val weekNumber = calculateWeekNumber(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
            calendarList.add(Pair(dayOfWeek,weekNumber))
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)
        }
        return calendarList
    }

    private fun enDayOfWeekToShortRus(enDayOfWeek: String): String {
        val engFull = res.stringArray(R.array.calendar_day_of_week_full_eng)
        val rusShort = res.stringArray(R.array.calendar_day_of_week_short_rus)
        return rusShort.getOrElse(engFull.indexOf(enDayOfWeek)){""}
    }

    private fun enMonthToShortRus(enMonth: String): String {
        val engFull = res.stringArray(R.array.calendar_month_full_eng)
        val rusShort = res.stringArray(R.array.calendar_month_short_rus)
        return rusShort.getOrElse(engFull.indexOf(enMonth)){""}
    }

    private fun enDayOfWeekToFullRus(enDayOfWeek: String): String {
        val engFull = res.stringArray(R.array.calendar_day_of_week_full_eng)
        val rusFull = res.stringArray(R.array.calendar_days_of_week_full_rus)
        return rusFull.getOrElse(engFull.indexOf(enDayOfWeek)){""}
    }

    private fun calculateWeekNumber(day: Int, month: Int, year: Int): Int {
        val calendarCurrent = Calendar.getInstance().apply {
            set(year, month, day)
        }
        val currentWeekNumber: Int = calendarCurrent.get(Calendar.WEEK_OF_YEAR)
        return if (currentWeekNumber > 28) {
            val calendarSeptemberCurrentYear = Calendar.getInstance().apply {
                set(year, Calendar.SEPTEMBER, 1)
            }
            val septemberWeekNumber = calendarSeptemberCurrentYear.get(Calendar.WEEK_OF_YEAR)
            when {
                (currentWeekNumber - septemberWeekNumber) >= 0 -> if ((currentWeekNumber - septemberWeekNumber + 1) % 4 == 0) 4 else (currentWeekNumber - septemberWeekNumber + 1) % 4
                (septemberWeekNumber - currentWeekNumber) % 2 == 0 -> if ((septemberWeekNumber - currentWeekNumber + 1) % 4 == 0) 4 else (septemberWeekNumber - currentWeekNumber + 1) % 4
                else -> if ((septemberWeekNumber - currentWeekNumber - 1) % 4 == 0) 4 else (septemberWeekNumber - currentWeekNumber - 1) % 4
            }
        } else {
            val calendarSeptemberLastYear = Calendar.getInstance().apply {
                set(year - 1, Calendar.SEPTEMBER, 1)
            }
            val weeksInLastYear = calendarSeptemberLastYear.getActualMaximum(Calendar.WEEK_OF_YEAR) + 1 - calendarSeptemberLastYear.get(Calendar.WEEK_OF_YEAR)
            if ((weeksInLastYear + currentWeekNumber) % 4 == 0) 4 else (weeksInLastYear + currentWeekNumber) % 4
        }
    }

}