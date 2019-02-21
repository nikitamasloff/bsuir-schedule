package com.nikitamaslov.bsuirschedule.ui.base

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils

abstract class BaseRecyclerView {

    abstract class Adapter<T> @JvmOverloads constructor(protected var items: List<T> = emptyList())
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        fun updateItems (listItems: List<T>) {
            this.items = listItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder{
            return viewHolder(LayoutInflater.from(parent.context)
                    .inflate(viewType, parent, false)
                    , viewType)
        }

        @Suppress("UNCHECKED_CAST")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as? Binder<T>)?.bind(this.items[position])
                    ?: throw IllegalStateException("viewholder must implement 'Binder'")
            setAnimation(holder.itemView,position)
        }

        override fun getItemCount(): Int {
            return this.items.size
        }

        override fun getItemViewType(position: Int): Int {
            return layoutResId(position, this.items[position])
        }

        /*
        * Animation
        */

        private var lastPosition: Int = -1

        private fun setAnimation(viewToAnimate: View, position: Int){
            if (position > lastPosition){
                val animation = AnimationUtils.loadAnimation(viewToAnimate.context, android.R.anim.fade_in)
                viewToAnimate.startAnimation(animation)
                lastPosition = position
            }
        }

        /*
        * Inheritance
        */

        @LayoutRes
        protected abstract fun layoutResId(position: Int, obj: T): Int

        protected abstract fun viewHolder(view: View, viewType: Int): RecyclerView.ViewHolder

    }

    interface Binder<T> {

        fun bind(data: T)

    }

}