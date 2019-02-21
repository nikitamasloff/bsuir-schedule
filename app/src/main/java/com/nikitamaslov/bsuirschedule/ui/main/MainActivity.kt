package com.nikitamaslov.bsuirschedule.ui.main

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Point
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nikitamaslov.bsuirschedule.R
import com.nikitamaslov.bsuirschedule.data.local.database.Database
import com.nikitamaslov.bsuirschedule.data.model.*
import com.nikitamaslov.bsuirschedule.ui.edit.EditActivity
import com.nikitamaslov.bsuirschedule.ui.palette.Palette
import com.nikitamaslov.bsuirschedule.ui.palette.PaletteItem
import com.nikitamaslov.bsuirschedule.ui.palette.PaletteMultiDialog
import com.nikitamaslov.bsuirschedule.ui.search.SearchActivity
import com.nikitamaslov.bsuirschedule.utils.HandlerThread
import com.nikitamaslov.bsuirschedule.utils.MultitypeSerializer
import com.nikitamaslov.bsuirschedule.utils.Resources
import com.polyak.iconswitch.IconSwitch
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import com.yarolegovich.mp.MaterialStandardPreference
import kotlinx.android.synthetic.main.layout_filter.*
import kotlinx.android.synthetic.main.toolbar_schedule.*


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener,
        IconSwitch.CheckedChangeListener, View.OnClickListener, ScheduleFragment.Callback {

    companion object {
        const val REQUEST_CODE_EDIT_ACTIVITY = 4
        const val REQUEST_CODE_SEARCH_ACTIVITY = 6

        private const val DURATION_COLOR_CHANGE_MS: Long = 400
    }

    //animation variables
    private val toolbarColors = IntArray(IconSwitch.Checked.values().size)
    private val statusBarColors = IntArray(toolbarColors.size)
    private lateinit var revealCenter: Point
    private lateinit var contentInInterpolator: AccelerateInterpolator
    private lateinit var contentOutInterpolator: DecelerateInterpolator
    private lateinit var statusBarAnimator: ValueAnimator
    private lateinit var filterLayout: View
    private lateinit var scheduleContainer: View
    private lateinit var toolbar: Toolbar

    private var needToReplaceFragment = false
    private lateinit var form: FilterModel
    private val iconSwitch: IconSwitch by lazy { schedule_icon_switch }

    private lateinit var thread: HandlerThread
    private lateinit var res: Resources
    private lateinit var database: Database.Proxy
    private lateinit var palette: Palette
    private lateinit var filterPrefs: FilterPreferences

    private val scheduleFragment: ScheduleFragment<*>
        get() = supportFragmentManager
        .findFragmentById(R.id.schedule_fragment_container)
            as ScheduleFragment<*>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init(this)
        initAnimation()
        replaceFragment()
        initToolbarListener()
        initFilter()
        initFilterListener()

        iconSwitch.setCheckedChangeListener(this)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

    }//onCreate

    override fun onPostResume() {
        super.onPostResume()
        if (needToReplaceFragment) {
            replaceFragment()
        }
    }

    override fun onPause() {
        super.onPause()
        scheduleFragment.onCancelDelete()
    }

    override fun onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        thread.quit()
        super.onDestroy()
    }

    private fun init(context: Context) {
        thread = HandlerThread().also { it.start() }
        res = Resources(context)
        database = Database.proxy(context)
        palette = Palette(res)
        filterPrefs = FilterPreferences(context)
        form = filterPrefs.form
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        android.R.id.home -> {
            launchSearchActivity()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() = when {
        iconSwitch.checked == IconSwitch.Checked.LEFT -> iconSwitch.toggle()
        scheduleFragment.onBackPressed() -> scheduleFragment.onCancelDelete()
        else -> super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_EDIT_ACTIVITY -> when (resultCode) {
                EditActivity.RESULT_EDIT -> {
                    val obj = MultitypeSerializer.deserialize(
                        Schedule::class,
                        data?.getStringExtra(EditActivity.EXTRA_DATA_OBJECT)
                    )
                    when (obj) {
                        is GroupSchedule -> {
                            needToReplaceFragment = true
                            thread.onWorker {
                                if (obj.group?.id != null) {
                                    database.update(obj)
                                }
                            }
                        }
                        is EmployeeSchedule -> {
                            needToReplaceFragment = true
                            thread.onWorker {
                                if (obj.employee?.id != null) {
                                    database.update(obj)
                                }
                            }
                        }
                    }
                }
                EditActivity.RESULT_ADD -> {
                    val obj = MultitypeSerializer.deserialize(
                        Schedule::class,
                        data?.getStringExtra(EditActivity.EXTRA_DATA_OBJECT)
                    )
                    when (obj) {
                        is GroupSchedule -> {
                            needToReplaceFragment = true
                            thread.onWorker {
                                if (obj.group != null && obj.group!!.id != null) {
                                    database.insert(obj)
                                }
                            }
                        }
                        is EmployeeSchedule -> {
                            needToReplaceFragment = true
                            thread.onWorker {
                                if (obj.employee != null && obj.employee!!.id != null) {
                                    database.insert(obj)
                                }
                            }
                        }
                    }
                }
            }
            REQUEST_CODE_SEARCH_ACTIVITY ->
                if (resultCode == SearchActivity.RESULT_CHANGED) {
                    needToReplaceFragment = true
            }
        }
    }

    override fun onCheckChanged(current: IconSwitch.Checked?) {
        updateColors(true)
        changeContentVisibility()
        when (current) {
            IconSwitch.Checked.RIGHT -> checkFilterUpdate()
            IconSwitch.Checked.LEFT -> {
                scheduleFragment.onCancelDelete()
                initFilter()
            }
        }
    }

    private fun initToolbarListener() {
        schedule_menu_delete.setOnClickListener {
            scheduleFragment.onDeletePressed()
        }
        schedule_menu_edit.setOnClickListener {
            scheduleFragment.onEditPressed()
        }
        schedule_menu_add.setOnClickListener {
            scheduleFragment.onAddPressed()
        }
    }

    private fun launchSearchActivity() {
        val intent = Intent(this, SearchActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SEARCH_ACTIVITY)
    }

    private fun setupTitle() {
        filterPrefs.selection.run {
            this@MainActivity.title = when (this) {
                is Group -> number
                is Employee -> fio
                else -> null
            }
        }
    }

    private fun replaceFragment() {
        setupTitle()
        needToReplaceFragment = false
        val fragment = filterPrefs.selection.run {
            when (this) {
                is Group -> ScheduleFragment.instance<GroupSchedule>(id)
                is Employee -> ScheduleFragment.instance<EmployeeSchedule>(id)
                else -> ScheduleFragment.instance(null)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.schedule_fragment_container, fragment)
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .commit()
        /*thread?.uiHandler?.postDelayed(
                {supportFragmentManager.beginTransaction()
                .replace(R.id.schedule_fragment_container, fragment)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .commit()}
        ,DURATION_COLOR_CHANGE_MS)*/
    }

    private fun checkFilterUpdate() {
        val f = filterPrefs.form
        if (form != f) {
            form = f
            replaceFragment()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        filterPrefs.run {
            when (key) {
                keySubgroup -> form.subgroup = sharedPreferences?.getInt(keySubgroup, 0) ?: 0
                keyIsColor -> form.isColor = sharedPreferences?.getBoolean(keyIsColor, false) ?: false
                keyIsCompleted -> form.isCompleted = sharedPreferences?.getBoolean(keyIsCompleted, true) ?: true
                keyIsScheduleTypeDefault -> form.isScheduleTypeDefault = sharedPreferences?.getBoolean(
                    keyIsScheduleTypeDefault,
                    true
                ) ?: true
                keyLabels -> form.labels = Gson().fromJson(
                    sharedPreferences?.getString(keyLabels, ""),
                    object : TypeToken<List<Int>?>() {}.type
                )
                keyLessonType -> form.lessonType = ArrayList(sharedPreferences?.getStringSet(keyLessonType, HashSet())
                    ?: List(0) { "" })
                this.key(this.selection) -> form.subjects = this.getSubjects(selection)
            }
        }
    }


    /*
    * Callback
    */

    override fun notifyDeleteEnabled() {
        val onAnimation = AlphaAnimation(0f, 1f).apply {
            duration = DURATION_COLOR_CHANGE_MS
        }
        schedule_menu_edit.apply {
            animation = onAnimation
            visibility = View.VISIBLE
        }
        schedule_menu_delete.apply {
            animation = onAnimation
            visibility = View.VISIBLE
        }
        /*schedule_menu_add.apply {
            animation = onAnimation
            visibility = View.VISIBLE
        }*/
        setupTitle()
    }

    override fun notifyDeleteOff() {
        val offAnimation = AlphaAnimation(1f, 0f).apply {
            duration = DURATION_COLOR_CHANGE_MS
        }
        scheduleFragment.onCancelDelete()
        schedule_menu_edit.apply {
            if (this.visibility != View.GONE) {
                animation = offAnimation
                visibility = View.GONE
            }
        }
        schedule_menu_delete.apply {
            if (this.visibility != View.GONE) {
                animation = offAnimation
                visibility = View.GONE
            }
        }
        /*schedule_menu_add.apply {
            if (this.visibility != View.GONE) {
                animation = offAnimation
                visibility = View.GONE
            }
        }*/
        thread.onUiDelayed({ setupTitle() }, DURATION_COLOR_CHANGE_MS)
    }

    override fun notifyEditEnabled() {
        val onAnimation = AlphaAnimation(0f, 1f).apply {
            duration = DURATION_COLOR_CHANGE_MS
        }
        schedule_menu_edit.apply {
            animation = onAnimation
            visibility = View.VISIBLE
        }
    }

    override fun notifyEditOff() {
        val offAnimation = AlphaAnimation(1f, 0f).apply {
            duration = DURATION_COLOR_CHANGE_MS
        }
        schedule_menu_edit.apply {
            if (this.visibility != View.GONE) {
                animation = offAnimation
                visibility = View.GONE
            }
        }
    }


    /*
    * Filter
    */

    private fun initFilter() {

        filter_current_color.visibility =
                if (filterPrefs.isColor)
                    View.VISIBLE
                else View.GONE

        filter_subjects.setSummary(
            filterPrefs.getSubjects(filterPrefs.selection)
                .toString().removePrefix("[").removeSuffix("]")
        )
        filter_lesson_type.setSummary(
            filterPrefs.lessonType
                ?.map { lessonTypeValueToDescriptionMulti(it) }
                ?.takeIf { it.size != lessonTypeValues.size }
                ?.toString()
                ?.removePrefix("[")?.removeSuffix("]")
                ?: getString(R.string.filter_item_lesson_type_def_value))
        filter_labels.setSummary(
            when {
                filterPrefs.labels == null || filterPrefs.labels?.size == palette.colors.size ->
                    R.string.filter_item_labels_def_value
                filterPrefs.labels?.isEmpty() == true ->
                    R.string.filter_item_labels_empty
                else -> R.string.filter_item_labels_middle
            }
        )
        filter_subgroup.setSummary(
            when (filterPrefs.subgroup) {
                1 -> subgroupDescriptions[1]
                2 -> subgroupDescriptions[2]
                else -> subgroupDescriptions[0]
            }
        )

        thread.onWorker {
            val sel = filterPrefs.selection
            val entryValues = when (sel) {
                is Group -> sel.id?.let { database.subjects(GroupSchedule::class, it) }
                is Employee -> sel.id?.let { database.subjects(EmployeeSchedule::class, it) }
                else -> emptyList()
            }
            thread.onUi {
                filter_subjects.setSummary(
                    when {
                        filterPrefs.getSubjects(sel) == null || filterPrefs.getSubjects(sel)?.size == entryValues?.size ->
                            res.string(R.string.filter_item_subjects_def_value)
                        filterPrefs.getSubjects(sel)?.isEmpty() ?: false ->
                            res.string(R.string.filter_item_subjects_empty)
                        else -> filterPrefs.getSubjects(filterPrefs.selection)?.joinToString(separator = ", ")
                    }
                )
            }
        }
    }

    private fun initFilterListener() {

        filter_preference_screen.setVisibilityController(
            filter_is_color_current,
            listOf(R.id.filter_current_color),
            false
        )

        filter_subjects.setOnClickListener(this)
        filter_labels.setOnClickListener(this)
        filter_lesson_type.setOnClickListener(this)
        filter_schedule_type.setOnClickListener(this)
        filter_subgroup.setOnClickListener(this)

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.filter_subjects -> (v as MaterialStandardPreference).showSubjectDialog()
            R.id.filter_lesson_type -> (v as MaterialStandardPreference).showLessonTypeDialog()
            R.id.filter_labels -> (v as MaterialStandardPreference).showLabelDialog()
            R.id.filter_schedule_type -> {
                filterPrefs.isScheduleDefault = !filterPrefs.isScheduleDefault
                thread.onUi { iconSwitch.toggle() }
            }
            R.id.filter_subgroup -> (v as MaterialStandardPreference).showSubgroupDialog()
        }
    }


    /*
    * Animation
    */

    private fun initAnimation() {
        initColors()
        initAnimationRelatedFields()
        updateColors(false)
    }

    private fun updateColors(animated: Boolean) {
        val colorIndex = iconSwitch.checked.ordinal
        toolbar.setBackgroundColor(toolbarColors[colorIndex])
        if (animated) {
            when (iconSwitch.checked) {
                IconSwitch.Checked.LEFT -> {
                    statusBarAnimator.reverse()
                }
                IconSwitch.Checked.RIGHT -> {
                    statusBarAnimator.start()
                }
                null -> {
                }
            }
            revealToolbar()
        } else window.statusBarColor = statusBarColors[colorIndex]
    }

    private fun revealToolbar() {
        iconSwitch.getThumbCenter(revealCenter)
        moveFromSwitchToToolbarSpace(revealCenter)
        ViewAnimationUtils.createCircularReveal(
            toolbar,
            revealCenter.x, revealCenter.y,
            iconSwitch.height.toFloat(), toolbar.width.toFloat()
        )
            .setDuration(DURATION_COLOR_CHANGE_MS)
            .start()
    }

    private fun changeContentVisibility() {

        var targetTranslation = 0
        var interpolator: Interpolator? = null
        when (iconSwitch.checked) {
            IconSwitch.Checked.LEFT -> {
                targetTranslation = scheduleContainer.height
                interpolator = contentOutInterpolator
            }
            IconSwitch.Checked.RIGHT -> {
                targetTranslation = 0
                interpolator = contentInInterpolator
            }
            null -> Unit
        }
        scheduleContainer.animate().cancel()
        scheduleContainer.animate()
            .translationY(targetTranslation.toFloat())
            .setDuration(DURATION_COLOR_CHANGE_MS)
            .setInterpolator(interpolator)
            .start()
    }

    private fun moveFromSwitchToToolbarSpace(point: Point) {
        point.set(point.x + iconSwitch.left, point.y + iconSwitch.top)
    }

    private fun initAnimationRelatedFields() {
        revealCenter = Point()
        statusBarAnimator = createArgbAnimator(
            statusBarColors[IconSwitch.Checked.LEFT.ordinal],
            statusBarColors[IconSwitch.Checked.RIGHT.ordinal]
        )
        contentInInterpolator = AccelerateInterpolator()
        contentOutInterpolator = DecelerateInterpolator()
        filterLayout = findViewById(R.id.filter_preference_screen)
        scheduleContainer = findViewById(R.id.schedule_fragment_container)

        toolbar = findViewById(R.id.schedule_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.toolbar_menu_black_24dp)
        }
    }

    private fun createArgbAnimator(leftColor: Int, rightColor: Int): ValueAnimator =
        ValueAnimator.ofArgb(leftColor, rightColor).apply {
            duration = DURATION_COLOR_CHANGE_MS
            addUpdateListener {
                window.statusBarColor = animatedValue as Int
            }
        }

    private fun initColors() {
        statusBarColors[IconSwitch.Checked.LEFT.ordinal] = res.color(R.color.colorMGray)
        statusBarColors[IconSwitch.Checked.RIGHT.ordinal] = res.color(R.color.color2DGray)
        toolbarColors[IconSwitch.Checked.LEFT.ordinal] = res.color(R.color.colorLGray)
        toolbarColors[IconSwitch.Checked.RIGHT.ordinal] = res.color(R.color.colorDGray)
    }

    /*
    * Dialogs
    */

    private fun MaterialStandardPreference.showSubjectDialog() {
        thread.onWorker {
            val selection = filterPrefs.selection
            val entryValues = when (selection) {
                is Group -> selection.id?.let { database.subjects(GroupSchedule::class, it) }
                is Employee -> selection.id?.let { database.subjects(EmployeeSchedule::class, it) }
                else -> return@onWorker
            }
            if (entryValues == null || entryValues.isEmpty()) return@onWorker
            val selectedValues = filterPrefs.getSubjects(selection)
            val selectedBooleanArray: List<Boolean> =
                when {
                    selectedValues == null -> List(entryValues.size) { true }
                    selectedValues.isEmpty() -> List(entryValues.size) { false }
                    else -> List(entryValues.size) { index -> selectedValues.contains(entryValues[index]) }
                }

            thread.onUi {
                LovelyChoiceDialog(context)
                    .setTopColorRes(R.color.lovely_dialog_top_color)
                    .setIcon(ContextCompat.getDrawable(context, R.drawable.filter_subjects_black_24dp))
                    .setItemsMultiChoice(
                        entryValues, selectedBooleanArray.toBooleanArray()
                    ) { _: MutableList<Int>?, items: MutableList<String>? ->
                        filterPrefs.setSubjects(selection, items)
                        this.setSummary(
                            when {
                                items?.size == entryValues.size && items.isNotEmpty() -> context.getString(R.string.filter_item_subjects_def_value)
                                items?.isEmpty() != false -> context.getString(R.string.filter_item_subjects_empty)
                                else -> items.toString().removePrefix("[").removeSuffix("]")
                            }
                        )
                    }
                    .show()
            }
        }
    }


    private fun MaterialStandardPreference.showLessonTypeDialog() {
        val entryDescriptions = context.resources.getStringArray(R.array.lesson_type_description_multi)
        val entryValues = context.resources.getStringArray(R.array.lesson_type_values)
        val selectedValues = filterPrefs.lessonType
        val selectedBooleanArray = when {
            selectedValues == null -> List(entryValues.size) { true }
            selectedValues.isEmpty() -> List(entryValues.size) { true }
            else -> List(entryValues.size) { index -> selectedValues.contains(entryValues[index]) }
        }
        LovelyChoiceDialog(context)
            .setTopColorRes(R.color.lovely_dialog_top_color)
            .setIcon(ContextCompat.getDrawable(context, R.drawable.filter_lesson_type_black_24dp))
            .setItemsMultiChoice(entryDescriptions, selectedBooleanArray.toBooleanArray())
            { positions: MutableList<Int>?, items: MutableList<String>? ->
                val defValue = context.getString(R.string.filter_item_lesson_type_def_value)
                if (positions == null) {
                    filterPrefs.lessonType = entryValues.toList()
                    this.setSummary(defValue)
                    return@setItemsMultiChoice
                }
                filterPrefs.lessonType = entryValues.slice(positions)
                this.setSummary(
                    when {
                        entryValues.size == positions.size -> {
                            defValue
                        }
                        positions.isEmpty() -> context.getString(R.string.filter_item_lesson_type_empty)
                        else -> items.toString().removePrefix("[").removeSuffix("]")
                    }
                )
            }.show()
    }


    private fun MaterialStandardPreference.showLabelDialog() {
        val paletteSize = palette.size
        val labels = filterPrefs.labels
        val selectedBooleanArray = when {
            labels == null || labels.size == paletteSize -> List(paletteSize) { true }
            labels.isEmpty() -> List(paletteSize) { false }
            else -> List(paletteSize) { index -> labels.contains(palette.colors[index]) }
        }.toBooleanArray()
        PaletteMultiDialog(context).init(selectedBooleanArray)
        { positions: MutableList<Int>?, items: MutableList<PaletteItem>? ->
            if (positions == null) {
                filterPrefs.labels = null
                return@init
            }
            filterPrefs.labels = palette.colors.slice(positions)
            this@showLabelDialog.setSummary(
                when {
                    items == null || palette.size == items.size ->
                        this@showLabelDialog.context.getString(R.string.filter_item_labels_def_value)
                    items.isEmpty() ->
                        this@showLabelDialog.context.getString(R.string.filter_item_labels_empty)
                    else -> this@showLabelDialog.context.getString(R.string.filter_item_labels_middle)
                }
            )
        }.show()
    }

    private fun MaterialStandardPreference.showSubgroupDialog() {
        LovelyChoiceDialog(context)
            .setItems(subgroupDescriptions) { _: Int?, item_: String? ->
                val value = subgroupDescriptionToValue(item_ ?: return@setItems)
                filterPrefs.subgroup = value
                this.setSummary(item_)
            }
            .setTitle(R.string.form_subgroup)
            .setIcon(ContextCompat.getDrawable(context, R.drawable.filter_subgroup_black_24dp))
            .setTopColorRes(R.color.lovely_dialog_top_color)
            .show()
    }

    /*
    * Resources
    */

    private val lessonTypeValues by lazy { res.stringArray(R.array.lesson_type_values) }

    private val subgroupDescriptions by lazy { res.stringArray(R.array.subgroup_description) }

    private fun lessonTypeValueToDescriptionMulti(value: String): String =
        res.stringArray(R.array.lesson_type_description_multi).getOrElse(lessonTypeValues.indexOf(value)){""}

    private fun subgroupDescriptionToValue(value: String): Int =
        res.stringArray(R.array.subgroup_values)
            .filter { item -> item.getOrElse(0) { return@filter false }.isDigit() && item.length == 1 }
            .map { it.toInt() }
            .getOrElse(subgroupDescriptions.indexOf(value)){0}

}