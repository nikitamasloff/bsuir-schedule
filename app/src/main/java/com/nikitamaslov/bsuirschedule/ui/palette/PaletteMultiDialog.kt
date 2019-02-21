package com.nikitamaslov.bsuirschedule.ui.palette

import android.content.Context
import com.nikitamaslov.bsuirschedule.utils.Resources
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import com.nikitamaslov.bsuirschedule.R

class PaletteMultiDialog (
        context: Context,
        private val res: Resources = Resources(context)
): LovelyChoiceDialog(context) {

    fun init(
        checked: BooleanArray,
        listener: PaletteMultiDialog.(positions: MutableList<Int>?, items: MutableList<PaletteItem>?) -> Unit
    ): PaletteMultiDialog = apply {
        setTopColorRes(R.color.lovely_dialog_top_color)
        setIcon(R.drawable.filter_labels_black_24dp)
        setItemsMultiChoice(
            PaletteAdapter(
                context = context,
                items = Palette(res).paletteItems,
                layoutResId = R.layout.item_simple_text_multichoice
            ),
            checked
        ) { positions_: MutableList<Int>?, items_: MutableList<PaletteItem>? ->
            listener(positions_,items_)
        }

    }

}
