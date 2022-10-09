package com.train.peripheral.mvi

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.train.peripheral.databinding.ModuleSampleItemCommLayoutBinding


/**
 * File: SampleAdapter.java
 * Author: yuzhuzhang
 * Create: 2022/1/15 10:20 PM
 *
 * -----------------------------------------------------------------
 * Description:
 *
 *
 * -----------------------------------------------------------------
 */
class SampleAdapter(private val listener: AdapterItemClickListener):
//class SampleAdapter(private val listener:(view:View,data:SampleMviData)-> Unit) :
    ListAdapter<SampleMviData, SampleAdapter.SampleMviHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleMviHolder {
        return SampleMviHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SampleMviHolder, position: Int) {
        holder.bindItemView(listener, getItem(position))
    }


     class SampleMviHolder : RecyclerView.ViewHolder {

        var databind: ModuleSampleItemCommLayoutBinding

        constructor(databind: ModuleSampleItemCommLayoutBinding) : super(databind.root) {
            this.databind = databind
        }

        companion object {

            fun from(parent: ViewGroup): SampleMviHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ModuleSampleItemCommLayoutBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                return SampleMviHolder(binding)
            }
        }


        fun bindItemView(listener: AdapterItemClickListener, item: SampleMviData) {
            databind.listener = listener
            databind.data = item
            databind.executePendingBindings()
        }
    }

    internal class TaskDiffCallback : DiffUtil.ItemCallback<SampleMviData>() {
        override fun areItemsTheSame(oldItem: SampleMviData, newItem: SampleMviData): Boolean {
            return TextUtils.equals(oldItem.name, newItem.name)
        }

        override fun areContentsTheSame(oldItem: SampleMviData, newItem: SampleMviData): Boolean {
            return TextUtils.equals(oldItem.name, newItem.name)
        }
    }
}


public interface AdapterItemClickListener {

    fun onItemClick(itemView: View, item: SampleMviData)

    fun onItemViewClick(itemView: View)

}

data class SampleMviData(var name: String, var age: Int)