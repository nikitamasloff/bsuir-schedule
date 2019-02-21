package com.nikitamaslov.bsuirschedule.ui.palette

import android.content.Context
import com.nikitamaslov.bsuirschedule.utils.Resources
import com.nikitamaslov.bsuirschedule.R
import com.yarolegovich.lovelydialog.LovelyChoiceDialog

class PaletteSingleDialog (
        context: Context,
        private val res: Resources = Resources(context)
): LovelyChoiceDialog(context) {

    fun init(listener: PaletteSingleDialog.(position: Int?, item: PaletteItem?) -> Unit): PaletteSingleDialog =
        apply {
            setTopColorRes(R.color.lovely_dialog_top_color)
            setIcon(R.drawable.filter_labels_black_24dp)
            setItems(
                PaletteAdapter(
                    context = context,
                    items = Palette(res).paletteItems,
                    layoutResId = R.layout.item_simple_text
                )
            ) { position_: Int?, item_: PaletteItem? ->
                listener(position_,item_)
            }

        }

}
