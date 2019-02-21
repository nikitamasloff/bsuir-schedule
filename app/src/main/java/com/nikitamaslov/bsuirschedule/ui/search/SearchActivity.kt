package com.nikitamaslov.bsuirschedule.ui.search

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.github.florent37.materialviewpager.MaterialViewPager
import com.nikitamaslov.bsuirschedule.R
import com.nikitamaslov.bsuirschedule.data.model.Employee
import com.nikitamaslov.bsuirschedule.data.model.Group
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrInterface


class SearchActivity : AppCompatActivity(), SearchFragment.Callback, SavedFragment.Callback {

    companion object {
        const val RESULT_CHANGED = 11
    }

    private val tabTitles: Array<String> by lazy { resources.getStringArray(R.array.search_tab_titles) }
    private val queryHints: Array<String> by lazy { resources.getStringArray(R.array.search_query_hints) }

    private lateinit var materialViewPager: MaterialViewPager
    private lateinit var adapter: MaterialAdapter

    private var searchView: SearchView? = null
    private var searchMenuItem: MenuItem? = null
    private var deleteMenuItem: MenuItem? = null
    private var refreshMenuItem: MenuItem? = null
    private var sortMenuItem: MenuItem? = null
    private var priorityIsGroupFirst = true

    private var slidr: SlidrInterface? = null

    private lateinit var savedFragment: SavedFragment
    private lateinit var searchGroupFragment: SearchFragment<Group>
    private lateinit var searchEmployeeFragment: SearchFragment<Employee>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        slidr = Slidr.attach(this@SearchActivity)

        materialViewPager = findViewById(R.id.search_material_view_pager)
        materialViewPager.viewPager.offscreenPageLimit = tabTitles.size - 1
        materialViewPager.viewPager.adapter = MaterialAdapter().also { adapter = it }
        materialViewPager.pagerTitleStrip.setViewPager(materialViewPager.viewPager)

        materialViewPager.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) = Unit

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

            override fun onPageSelected(position: Int) {
                updateMenu(position)
                when (position) {
                    0 -> savedFragment.onCancelEdit()
                    1 -> {
                        savedFragment.onCancelEdit()
                        searchView?.setOnQueryTextListener(searchGroupFragment as SearchView.OnQueryTextListener)
                    }
                    2 -> searchView?.setOnQueryTextListener(searchEmployeeFragment as SearchView.OnQueryTextListener)
                }
            }

        })

        title = null
        setSupportActionBar(materialViewPager.toolbar)

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        searchMenuItem = menu?.findItem(R.id.search_menu_find)
        searchView = searchMenuItem?.actionView as SearchView

        deleteMenuItem = menu?.findItem(R.id.search_menu_delete)
        refreshMenuItem = menu?.findItem(R.id.search_menu_refresh)
        sortMenuItem = menu?.findItem(R.id.search_menu_priority)

        val onMenuItemClickListener = MenuItem.OnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.search_menu_priority -> savedFragment.onPriorityPressed(!priorityIsGroupFirst)
                    .also { priorityIsGroupFirst = !priorityIsGroupFirst }
                R.id.search_menu_refresh -> savedFragment.onUpdatePressed()
                R.id.search_menu_delete -> savedFragment.onDeletePressed()
            }
            true
        }

        if (materialViewPager.viewPager.currentItem == 0) slidr?.unlock()

        deleteMenuItem?.setOnMenuItemClickListener(onMenuItemClickListener)
        refreshMenuItem?.setOnMenuItemClickListener(onMenuItemClickListener)
        sortMenuItem?.setOnMenuItemClickListener(onMenuItemClickListener)

        updateMenu(materialViewPager.viewPager.currentItem)

        return true
    }


    private fun updateMenu(page: Int) {
        searchView?.clearFocus()
        searchView?.setQuery("", false)
        enableDelete(false)
        setQueryHint(queryHints[page])
        when (page) {
            0 -> {
                slidr?.unlock()
                enableSearch(false)
                enableRefresh(true)
                enableSort(true)
            }
            1 -> {
                slidr?.lock()
                enableRefresh(false)
                enableSort(false)
                enableSearch(true)
            }
            2 -> {
                slidr?.lock()
                enableRefresh(false)
                enableSort(false)
                enableSearch(true)
            }
        }

    }


    override fun onBackPressed() {
        if (savedFragment.onCancelEdit()) {
            return
        } else {
            super.onBackPressed()
        }
    }


    /*
    * Callback
    */

    override fun onNewScheduleAdded() {
        savedFragment.onUpdatePressed()
    }

    override fun notifyEditOn() {
        enableDelete(true)
        enableRefresh(false)
        enableSort(false)
    }

    override fun notifyEditOff() {
        enableDelete(false)
        if (materialViewPager.viewPager.currentItem == 0) {
            enableRefresh(true)
            enableSort(true)
        }
    }


    private fun setQueryHint(hint: String) {
        searchView?.setQuery("", false)
        searchView?.queryHint = hint
    }

    private fun enableSearch(enable: Boolean) {
        searchMenuItem?.isVisible = enable
    }

    private fun enableDelete(enable: Boolean) {
        deleteMenuItem?.isVisible = enable
    }

    private fun enableRefresh(enable: Boolean) {
        refreshMenuItem?.isVisible = enable
    }

    private fun enableSort(enable: Boolean) {
        sortMenuItem?.isVisible = enable
    }

    private inner class MaterialAdapter : FragmentPagerAdapter(supportFragmentManager) {

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> SavedFragment.instance().also { savedFragment = it }
            1 -> SearchFragment.instance<Group>().also { searchGroupFragment = it }
            2 -> SearchFragment.instance<Employee>().also { searchEmployeeFragment = it }
            else -> throw IndexOutOfBoundsException("invalid page number")
        }

        override fun getPageTitle(position: Int): CharSequence? = tabTitles[position]

        override fun getCount(): Int = tabTitles.size

    }

}