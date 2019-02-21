package com.nikitamaslov.bsuirschedule.ui.edit

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.MultiAutoCompleteTextView
import com.nikitamaslov.bsuirschedule.R
import com.nikitamaslov.bsuirschedule.data.local.database.Database
import com.nikitamaslov.bsuirschedule.data.model.*
import com.nikitamaslov.bsuirschedule.ui.palette.Palette
import com.nikitamaslov.bsuirschedule.ui.palette.PaletteSingleDialog
import com.nikitamaslov.bsuirschedule.utils.HandlerThread
import com.nikitamaslov.bsuirschedule.utils.MultitypeSerializer
import com.nikitamaslov.bsuirschedule.utils.Resources
import com.nikitamaslov.bsuirschedule.utils.extension.duration
import com.nikitamaslov.bsuirschedule.utils.extension.snackbar
import com.nikitamaslov.bsuirschedule.utils.extension.text
import com.r0adkll.slidr.Slidr
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView
import com.rengwuxian.materialedittext.MaterialMultiAutoCompleteTextView
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import kotlinx.android.synthetic.main.activity_edit.*
import java.util.*

class EditActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val RESULT_ADD = 13
        const val RESULT_EDIT = 98
        const val EXTRA_DATA_OBJECT = "extra_data_object"
        const val EXTRA_DATA_KEY = "type_key"
    }

    private val extraDataObject: Schedule? by lazy {
        MultitypeSerializer.deserialize(Schedule::class, intent.getStringExtra(EXTRA_DATA_OBJECT))
    }

    private val extraDataKey: Host? by lazy {
        MultitypeSerializer.deserialize(Host::class, intent.getStringExtra(EXTRA_DATA_KEY))
    }

    private var employeeList: List<Employee> = ArrayList()
    private var groupList: List<Group> = ArrayList()
    private var auditoryList: List<Auditory> = ArrayList()

    private val subject by lazy { form_subject }
    private val lessonType by lazy { form_lesson_type }
    private val subgroup by lazy { form_subgroup }
    private val weekDay by lazy { form_week_day }
    private val weekNumber by lazy { form_week_number }
    private val startTime by lazy { form_start_time }
    private val endTime by lazy { form_end_time }

    private val groupCategory by lazy { form_category_group }
    private val groupCategoryTitle by lazy { form_category_group_title }
    private val group by lazy { form_group }

    private val employeeCategory by lazy { form_category_employee }
    private val employeeSurname by lazy { form_employee_surname }
    private val employeeName by lazy { form_employee_name }
    private val employeePatronymic by lazy { form_employee_patronymic }

    private val auditory by lazy { form_auditory }
    private val note by lazy { form_note }

    private val form = FormModel()

    private lateinit var thread: HandlerThread
    private lateinit var res: Resources
    private lateinit var database: Database.Proxy
    private lateinit var palette: Palette


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        Slidr.attach(this)

        init(this)

        title = if (extraDataObject != null) getString(R.string.form_toolbar_edit)
        else getString(R.string.form_toolbar_add)

        initForm()
        initListeners()

    }

    private fun init(context: Context) {
        thread = HandlerThread().also { it.start() }
        res = Resources(context)
        database = Database.proxy(context)
        palette = Palette(res)
    }

    override fun onDestroy() {
        thread.quit()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.edit_ok) {
            onAccept()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initForm() = extraDataObject.let {
        when (it) {
            is GroupSchedule -> {

                //key
                form.group = extraDataKey as? Group

                groupCategory.visibility = View.GONE
                initEmployee(it.employee)
                form.employee = it.employee

                subject.setText(it.subject)
                form.subject = it.subject

                it.lessonType?.run {
                    val value = lessonTypeValueToDescriptionSingle(this)
                    if (value.isNotEmpty()) {
                        lessonType.text = value
                    }
                }
                form.lessonType = it.lessonType

                subgroup.text = subgroupValueToDescription(it.subgroup ?: 0)
                form.subgroup = it.subgroup

                weekDay.text = it.dayOfWeek
                form.weekDay = it.dayOfWeek

                weekNumber.text = weekNumberToString(it.numberOfWeek)
                form.weekNumber = it.numberOfWeek

                startTime.text = it.startTime
                form.startTime = it.startTime

                endTime.text = it.endTime
                form.endTime = it.endTime

                auditory.setText(auditoryToString(it.auditory))
                form.auditory = it.auditory

                note.setText(it.note)
                form.note = it.note

                form.color = it.color
                setColor(form.color)

            }
            is EmployeeSchedule -> {

                //key
                form.employee = extraDataKey as? Employee

                employeeCategory.visibility = View.GONE
                initGroup(it.group)
                form.studentGroupList = it.group

                subject.setText(it.subject)
                form.subject = it.subject

                it.lessonType?.run {
                    val value = lessonTypeValueToDescriptionSingle(this)
                    if (value.isNotEmpty()) {
                        lessonType.text = value
                    }
                }
                form.lessonType = it.lessonType

                subgroup.text = subgroupValueToDescription(it.subgroup ?: 0)
                form.subgroup = it.subgroup

                weekDay.text = it.dayOfWeek
                form.weekDay = it.dayOfWeek

                weekNumber.text = weekNumberToString(it.numberOfWeek)
                form.weekNumber = it.numberOfWeek

                startTime.text = it.startTime
                form.startTime = it.startTime

                endTime.text = it.endTime
                form.endTime = it.endTime

                auditory.setText(auditoryToString(it.auditory))
                form.auditory = it.auditory

                note.setText(it.note)
                form.note = it.note

                form.color = it.color
                setColor(form.color)

            }
            null -> {
                when (extraDataKey) {
                    is Group -> {
                        form.group = extraDataKey as Group
                        groupCategory.visibility = View.GONE
                    }
                    is Employee -> {
                        form.employee = extraDataKey as Employee
                        employeeCategory.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun initEmployee(employee: Employee?) {
        employee?.surname?.let { employeeSurname.setText(it) }
        employee?.name?.let { employeeName.setText(it) }
        employee?.patronymic?.let { employeePatronymic.setText(it) }
    }

    private fun initGroup(groups: List<String>?) {
        groups?.let {
            group.setText(studentGroupToString(it))
            if (it.size < 2) groupCategoryTitle.text = getString(R.string.form_group)
            else groupCategoryTitle.text = getString(R.string.form_groups)
        }
    }


    private fun initListeners() {
        form_lesson_type_layout.setOnClickListener(this)
        form_subgroup_layout.setOnClickListener(this)
        form_week_day_layout.setOnClickListener(this)
        form_week_number_layout.setOnClickListener(this)
        form_start_time_layout.setOnClickListener(this)
        form_end_time_layout.setOnClickListener(this)
        form_color_layout.setOnClickListener(this)
        form_note_layout.setOnClickListener(this)
        form_subject_layout.setOnClickListener(this)
        form_group_layout.setOnClickListener(this)
        form_employee_surname_layout.setOnClickListener(this)
        form_employee_name_layout.setOnClickListener(this)
        form_employee_patronymic_layout.setOnClickListener(this)
        form_auditory_layout.setOnClickListener(this)

        form_group.addOnMultiAutoCompleteTextChangeListener()
        form_employee_surname.addOnAutoCompleteTextChangeListener()
        form_auditory.addOnMultiAutoCompleteTextChangeListener()

        thread.onWorker {
            employeeList = database.query(Employee::class).sortedBy { it.fio }
            thread.onUi {
                form_employee_surname.addOnAutoCompleteTextChangeListener()
            }
        }

        thread.onWorker {
            groupList = database.query(Group::class).sortedBy { it.number }
            thread.onUi {
                form_group.addOnMultiAutoCompleteTextChangeListener()
            }
        }

        thread.onWorker {
            auditoryList = database.query(Auditory::class).sortedBy { it.toString() }
            thread.onUi {
                form_auditory.addOnMultiAutoCompleteTextChangeListener()
            }
        }

    }

    override fun onClick(v: View?) {
        when (v) {
            form_subject_layout -> subject.requestFocus()
            form_group_layout -> group.requestFocus()
            form_employee_name_layout -> employeeName.requestFocus()
            form_employee_surname_layout -> employeeSurname.requestFocus()
            form_employee_patronymic_layout -> employeePatronymic.requestFocus()
            form_auditory_layout -> auditory.requestFocus()
            form_note_layout -> note.requestFocus()

            form_lesson_type_layout -> showLessonTypeDialog(this) {
                form.lessonType = it
                lessonType.text = lessonTypeValueToDescriptionSingle(it ?: return@showLessonTypeDialog)
            }
            form_subgroup_layout -> showSubgroupDialog(this) {
                form.subgroup = it ?: 0
                subgroup.text = subgroupValueToDescription(it ?: return@showSubgroupDialog)
            }
            form_week_day_layout -> {
                v?.setBackgroundColor(Color.TRANSPARENT)
                showWeekDayDialog(this) {
                    form.weekDay = it
                    weekDay.text = it ?: return@showWeekDayDialog
                }
            }
            form_week_number_layout -> showWeekNumberDialog(
                this,
                List(4) { index -> form.weekNumber?.contains(index + 1) == true }.toBooleanArray()
            ) {
                form.weekNumber = if (it?.containsAll(weekNumbers) == true)
                    listOf(0, *weekNumbers.toTypedArray()) else it
                weekNumber.text = it?.toString()?.removeSuffix("]")?.removePrefix("[") ?: return@showWeekNumberDialog
            }
            form_start_time_layout -> {
                checkTime(false)
                showTimePickerDialog(
                    this, R.string.form_start_time,
                    hoursFromString(form.startTime),
                    minutesFromString(form.startTime)
                )
                { hourOfDay, minute ->
                    form.startTime = time(hourOfDay, minute)
                    startTime.text = form.startTime
                    checkTime(false)
                }

            }
            form_end_time_layout -> {
                checkTime(false)
                showTimePickerDialog(
                    this, R.string.form_start_time,
                    hoursFromString(form.endTime),
                    minutesFromString(form.endTime)
                )
                { hourOfDay, minute ->
                    form.endTime = time(hourOfDay, minute)
                    endTime.text = form.endTime
                    checkTime(false)
                }
            }
            form_color_layout -> {
                PaletteSingleDialog(this)
                    .init { position, item ->
                        form.color = item?.color ?: 0
                        setColor(form.color)
                    }.show()
            }
        }
    }

    private fun MaterialAutoCompleteTextView.addOnAutoCompleteTextChangeListener() {
        if (id == R.id.form_employee_surname) {
            this.setAdapter(ArrayAdapter<String>(this@EditActivity,
                android.R.layout.simple_dropdown_item_1line,
                employeeList.map { it.fio }
            ))
            this.setOnItemClickListener { parent, view, position, id ->
                var employee: Employee? = null
                val selected = parent.getItemAtPosition(position) as? String
                val pos = employeeList.map { it.fio }.indexOf(selected)
                for (item in employeeList) {
                    if (item.fio == employeeList[pos].fio) {
                        employee = employeeList[pos]
                    }
                }
                employee?.let {
                    form.employee = employee
                    employeeSurname.setText(form.employee?.surname ?: "")
                    employeeName.setText(form.employee?.name ?: "")
                    employeePatronymic.setText(form.employee?.patronymic ?: "")
                }
            }
            this.threshold = 1
            this.dropDownHorizontalOffset = -(this.width * 0.1).toInt()
        }
    }

    private fun MaterialMultiAutoCompleteTextView.addOnMultiAutoCompleteTextChangeListener() {
        when (id) {
            R.id.form_group -> {
                this.setAdapter(
                    ArrayAdapter<String>(this@EditActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        groupList.map { it.number })
                )
                this.setOnItemClickListener { parent, view, position, id ->
                    var group: Group? = null
                    val selected = parent.getItemAtPosition(position) as? String
                    val pos = groupList.map { it.number }.indexOf(selected)
                    for (item in groupList) {
                        if (item.number == groupList[pos].number) {
                            group = groupList[pos]
                        }
                    }
                    form.studentGroupList = if (form.studentGroupList != null)
                        ArrayList(form.studentGroupList ?: emptyList()).apply { add(group?.number) }
                    else ArrayList(listOf(group?.number ?: ""))
                }
                this.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
                this.threshold = 1
                this.dropDownHorizontalOffset = -(this.width * 0.1).toInt()
            }
            R.id.form_auditory -> {
                this.setAdapter(
                    ArrayAdapter<String>(this@EditActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        auditoryList.map { "${it.number}-${it.pavilion}" })
                )
                this.setOnItemClickListener { parent, view, position, id ->
                    var auditory: Auditory? = null
                    val selected = parent.getItemAtPosition(position) as? String
                    val pos = auditoryList.map { "${it.number}-${it.pavilion}" }.indexOf(selected)
                    for (item in auditoryList) {
                        if (item.number == auditoryList[pos].number) {
                            auditory = auditoryList[pos]
                        }
                    }
                    form.auditory = if (form.auditory != null)
                        ArrayList(
                            form.auditory ?: emptyList()
                        ).apply { add(auditory?.let { "${it.number}-${it.pavilion}" }) }
                    else ArrayList(listOf(auditory?.let { "${it.number}-${it.pavilion}" } ?: ""))
                }
                this.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
                this.threshold = 1
                this.dropDownHorizontalOffset = -(this.width * 0.1).toInt()
            }
        }
    }

    private fun validate(): Boolean {
        fillForm()
        val check1: Boolean = checkTime(true)
        val check2: Boolean = checkWeekDay()
        return check1 && check2
    }

    private fun fillForm() {
        form.subject = subject.text.toString()
        form.employee?.surname = employeeSurname.text.toString()
        form.employee?.name = employeeName.text.toString()
        form.employee?.patronymic = employeePatronymic.text.toString()
        form.studentGroupList = removeRedundant(group.text.toString())
            ?.split(",")?.filter { it.isNotBlank() }
        form.auditory = removeRedundant(auditory.text.toString())
            ?.split(",")?.filter { it.isNotBlank() }
        form.note = note.text.toString()
    }

    private fun checkTime(onAccept: Boolean): Boolean {
        var counter = true
        if (onAccept && (form.startTime == null || form.startTime?.isEmpty() != false)) {
            form_start_time_layout.setBackgroundColor(ContextCompat.getColor(this@EditActivity, R.color.form_error))
            counter = false
        } else {
            form_start_time_layout.setBackgroundColor(Color.TRANSPARENT)
        }
        if (onAccept && (form.endTime == null || form.endTime?.isEmpty() != false)) {
            form_end_time_layout.setBackgroundColor(ContextCompat.getColor(this@EditActivity, R.color.form_error))
            counter = false
        } else {
            form_end_time_layout.setBackgroundColor(Color.TRANSPARENT)
        }
        if (!counter)
            return false
        return if (isTimeError(form.startTime, form.endTime)) {
            form_start_time_layout.setBackgroundColor(ContextCompat.getColor(this@EditActivity, R.color.form_error))
            form_end_time_layout.setBackgroundColor(ContextCompat.getColor(this@EditActivity, R.color.form_error))
            form_start_time.snackbar {
                text(getString(R.string.form_time_error))
                duration(Snackbar.LENGTH_LONG)
            }
            false
        } else {
            form_start_time_layout.setBackgroundColor(Color.TRANSPARENT)
            form_end_time_layout.setBackgroundColor(Color.TRANSPARENT)
            true
        }
    }

    private fun checkWeekDay(): Boolean {
        return if (form.weekDay == null || form.weekDay!!.isEmpty()) {
            form_week_day_layout.setBackgroundColor(ContextCompat.getColor(this@EditActivity, R.color.form_error))
            false
        } else {
            form_week_day_layout.setBackgroundColor(Color.TRANSPARENT)
            true
        }
    }

    private fun onAccept() {

        if (!validate()) {
            return
        }

        val data: Schedule? = when (extraDataObject) {
            is GroupSchedule, is EmployeeSchedule -> extraDataObject
            null -> {
                when (extraDataKey) {
                    is Group -> GroupSchedule()
                    is Employee -> EmployeeSchedule()
                    else -> null
                }
            }
        }

        when (data) {
            is GroupSchedule -> {
                data.subject = form.subject
                data.lessonType = form.lessonType
                data.subgroup = form.subgroup
                data.dayOfWeek = form.weekDay
                data.numberOfWeek = form.weekNumber ?: listOf(0)
                data.startTime = form.startTime
                data.endTime = form.endTime
                data.employee = form.employee
                data.auditory = form.auditory ?: emptyList()
                data.note = form.note
                data.color = form.color
                data.group = form.group
            }
            is EmployeeSchedule -> {
                data.subject = form.subject
                data.lessonType = form.lessonType
                data.subgroup = form.subgroup
                data.dayOfWeek = form.weekDay
                data.numberOfWeek = form.weekNumber ?: listOf(0)
                data.startTime = form.startTime
                data.endTime = form.endTime
                data.group = form.studentGroupList ?: emptyList()
                data.auditory = form.auditory ?: emptyList()
                data.note = form.note
                data.color = form.color
                data.employee = form.employee
            }
        }

        val intent = Intent().apply {
            putExtra(EXTRA_DATA_OBJECT, MultitypeSerializer.serialize(data))
        }

        val result = if (extraDataObject == null) RESULT_ADD else RESULT_EDIT
        setResult(result, intent)
        finish()
    }

    private fun setColor(color: Int?) {
        form_color.setBackgroundColor(color ?: return)
        val index = palette.colors.indexOf(color)
        palette.textColors.getOrNull(index)?.let {
            form_color_wrapper.setBackgroundColor(it)
        }
    }

    /*
    * Utils
    */

    private fun showSubgroupDialog(context: Context?, listener: (Int?) -> Unit) {
        LovelyChoiceDialog(context)
            .setItems(subgroupDescriptions) { position_: Int?, item_: String? ->
                listener(subgroupDescriptionToValue(item_ ?: return@setItems))
            }
            .setTitle(R.string.form_subgroup)
            .setTopColorRes(R.color.lovely_dialog_top_color)
            .show()
    }

    private fun showLessonTypeDialog(context: Context?, listener: (String?) -> Unit) {
        LovelyChoiceDialog(context)
            .setItems(lessonTypeDescriptionsSingle) { position_: Int, item_: String? ->
                listener(lessonTypeDescriptionToValue(item_ ?: return@setItems))
            }
            .setTopColorRes(R.color.lovely_dialog_top_color)
            .setTitle(R.string.form_lesson_type)
            .show()
    }

    private fun showWeekDayDialog(context: Context?, listener: (String?) -> Unit) {
        LovelyChoiceDialog(context)
            .setItems(weekDays) { position_: Int, item_: String? ->
                listener(item_)
            }
            .setTitle(R.string.form_day_of_week)
            .setTopColorRes(R.color.lovely_dialog_top_color)
            .show()
    }

    private fun showWeekNumberDialog(context: Context?, checked: BooleanArray, listener: (items: List<Int>?) -> Unit) {
        LovelyChoiceDialog(context)
            .setItemsMultiChoice(
                List(4) { index -> index + 1 },
                checked
            ) { positions_: MutableList<Int>?, items_: MutableList<Int>? ->
                listener(items_)
            }
            .setTitle(R.string.form_week_number)
            .setTopColorRes(R.color.lovely_dialog_top_color)
            .show()
    }

    private val calendar = Calendar.getInstance()
    private val curHours = calendar.get(Calendar.HOUR_OF_DAY)
    private val curMinutes = calendar.get(Calendar.MINUTE)

    private fun showTimePickerDialog(
        context: Context?,
        @StringRes title: Int,
        initHours: Int? = curHours,
        initMinutes: Int? = curMinutes,
        listener: (hourOfDay: Int, minute: Int) -> Unit
    ) {
        val minutes = initMinutes ?: curMinutes
        val hours = initHours ?: curHours
        val timePicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { view_, hourOfDay_, minute_ ->
            listener(hourOfDay_, minute_)
        }, hours, minutes, true)
        timePicker.setTitle(title)
        timePicker.show()
    }

    private fun minutesFromString(minutes: String?): Int? = minutes?.takeIf { item ->
        item.getOrElse(3) { return@takeIf false }.isDigit()
                && item.getOrElse(4) { return@takeIf false }.isDigit()
    }
        ?.substring(3..4)
        ?.toInt()

    private fun hoursFromString(hours: String?): Int? = hours
        ?.takeIf { item ->
            item
                .getOrElse(0) { return@takeIf false }
                .isDigit() && item.getOrElse(1) { return@takeIf false }.isDigit()
        }
        ?.substring(0..1)
        ?.toInt()

    private fun isTimeError(start: String?, end: String?): Boolean {
        val hours1 = hoursFromString(start) ?: return false
        val hours2 = hoursFromString(end) ?: return false
        val minutes1 = minutesFromString(start) ?: return false
        val minutes2 = minutesFromString(end) ?: return false
        return (hours1 > hours2 || (hours1 == hours2 && minutes1 > minutes2))
    }

    private fun time(hours: Int?, minutes: Int?): String? {
        if (hours == null || minutes == null) return null
        val h = if (hours > 9) hours.toString() else "0$hours"
        val m = if (minutes > 9) minutes.toString() else "0$minutes"
        return "$h:$m"
    }

    private fun removeRedundant(string: String?): String? {
        if (string == null || string.isEmpty()) return string
        val commaIndex: Int = string.indexOfLast { c -> c == ',' }
        if (commaIndex == -1) return string
        for (index in (commaIndex + 1)..(string.length - 1)) {
            if (string[index] != ' ') return string
        }
        return string.substring(0..commaIndex)
    }
    
    /*
    * Resources
    */

    private val lessonTypeDescriptionsMulti by lazy { res.stringArray(R.array.lesson_type_description_multi) }

    private val lessonTypeDescriptionsSingle by lazy { res.stringArray(R.array.lesson_type_description_single) }
    
    private val subgroupDescriptions by lazy { res.stringArray(R.array.subgroup_description) }
    
    private val weekDays by lazy { res.stringArray(R.array.calendar_days_of_week_full_rus) }

    private val weekNumbers by lazy { listOf(1,2,3,4) }
    
    private val lessonTypeValues by lazy { res.stringArray(R.array.lesson_type_values) }
    
    private val subgroupValues by lazy {
        res.stringArray(R.array.subgroup_values)
            .filter { item -> item.getOrElse(0) { return@filter false }.isDigit() && item.length == 1 }
            .map { it.toInt() }
    }

    private fun lessonTypeDescriptionToValue(value: String): String =
        lessonTypeValues.getOrNull(lessonTypeDescriptionsMulti.indexOf(value))
            ?: lessonTypeValues.getOrNull(res.stringArray(R.array.lesson_type_description_single).indexOf(value)) ?: ""

    private fun lessonTypeValueToDescriptionSingle(value: String): String =
        res.stringArray(R.array.lesson_type_description_single).getOrElse(lessonTypeValues.indexOf(value)){""}

    private fun subgroupValueToDescription(value: Int): String =
        subgroupDescriptions.getOrElse(subgroupValues.indexOf(value)){""}

    private fun subgroupDescriptionToValue(value: String): Int =
        res.stringArray(R.array.subgroup_values)
            .filter { item -> item.getOrElse(0) { return@filter false }.isDigit() && item.length == 1 }
            .map { it.toInt() }
            .getOrElse(subgroupDescriptions.indexOf(value)){0}

    private fun weekNumberToString(value: List<Int>): String =
        value.takeIf { !it.contains(0) && !it.containsAll(listOf(1,2,3,4)) }?.toString()?.removePrefix("[")?.removeSuffix("]")
            ?: res.string(R.string.week_number_default)

    private fun studentGroupToString(list: List<String>): String {
        return list.toString().removePrefix("[").removeSuffix("]")
    }

    private fun auditoryToString(list: List<String>?): String {
        return list?.toString()?.removePrefix("[")?.removeSuffix("]") ?: ""
    }

    private class FormModel {

        var subject: String? = null
        var lessonType: String? = null
        var subgroup: Int? = 0
            set(value) {
                field = when (value){
                    1,2 -> value
                    else -> 0
                }
            }
        var weekDay: String? = null
        var weekNumber: List<Int>? = null
        var startTime: String? = null
        var endTime: String? = null

        var employee: Employee? = null
        var group: Group? = null
        var studentGroupList: List<String>? = null

        var auditory: List<String>? = null
        var note: String? = null
        var color: Int? = 0

    }

}