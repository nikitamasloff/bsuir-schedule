package com.nikitamaslov.bsuirschedule.ui.palette

import android.content.Context
import com.nikitamaslov.bsuirschedule.utils.Resources
import com.nikitamaslov.bsuirschedule.R

class Palette (private val res: Resources) {

    constructor(context: Context) : this(Resources(context))

    companion object {

        private val blackTextColorIndices = listOf(10, 11, 12, 13, 14, 17, 19)

    }

    val size = 21

    val colors: List<Int> by lazy {
        listOf(
            res.color(R.color.palette_red),
            res.color(R.color.palette_pink),
            res.color(R.color.palette_purple),
            res.color(R.color.palette_deep_purple),
            res.color(R.color.palette_indigo),
            res.color(R.color.palette_blue),
            res.color(R.color.palette_light_blue),
            res.color(R.color.palette_cyan),
            res.color(R.color.palette_teal),
            res.color(R.color.palette_green),
            res.color(R.color.palette_light_green),
            res.color(R.color.lime),
            res.color(R.color.palette_yellow),
            res.color(R.color.palette_amber),
            res.color(R.color.palette_orange),
            res.color(R.color.palette_deep_orange),
            res.color(R.color.palette_brown),
            res.color(R.color.palette_grey),
            res.color(R.color.palette_blue_grey),
            res.color(R.color.palette_white),
            res.color(R.color.palette_black)
        ).also { require(it.size == size) }
    }

    val titles: List<String> by lazy { res.stringArray(R.array.palette).map(String::capitalize) }

    val textColors: List<Int> by lazy {
        List(size) { index ->
            when (blackTextColorIndices.contains(index)) {
                true -> black
                false -> white
            }
        }
    }

    val paletteItems: List<PaletteItem> by lazy {
        List(size) { index: Int ->
            PaletteItem(colors[index], titles[index], textColors[index])
        }
    }

    private val white by lazy { res.color(R.color.palette_white) }
    private val black by lazy { res.color(R.color.palette_black) }

}