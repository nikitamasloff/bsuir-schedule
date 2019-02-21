package com.nikitamaslov.bsuirschedule.ui.palette

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class PaletteAdapter(
    context: Context,
    private val items: List<PaletteItem>,
    @LayoutRes private val layoutResId: Int)
    : ArrayAdapter<PaletteItem>(
    context,
    com.yarolegovich.lovelydialog.R.layout.item_simple_text_multichoice,
    items
) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(layoutResId, parent, false)
        }

        val item = items[position]

        view!!.setBackgroundColor(item.color)
        val text = view.findViewById<TextView>(android.R.id.text1)!!
        text.text = item.title
        text.setTextColor(item.textColor)

        return view
    }

}
