package com.train.peripheral.ui

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.train.peripheral.databinding.ModuleHomeItemCommLayoutBinding
import org.ble.scan.ScanResult

/**
 * Author: yuzzha
 * Date: 2021/12/10 11:56
 * Description:
 * Remark:
 */
class ScanAdapter(val listener: AdapterListener) :
    ListAdapter<ScanResult, ScanAdapter.ScanViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        return ScanViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        holder.bindItemView(listener,getItem(position))
    }


    class ScanViewHolder : RecyclerView.ViewHolder {

        var databind: ModuleHomeItemCommLayoutBinding

        constructor(databind: ModuleHomeItemCommLayoutBinding) : super(databind.root) {
            this.databind = databind
        }

        companion object {

            fun from(parent: ViewGroup): ScanViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ModuleHomeItemCommLayoutBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                return ScanViewHolder(binding)
            }
        }


        fun bindItemView(listener: AdapterListener, item: ScanResult) {
            databind.listener = listener
            databind.device = item
            databind.executePendingBindings()
        }
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<ScanResult>() {
    override fun areItemsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
        return TextUtils.equals(oldItem.device.name, newItem.device.name)
    }

    override fun areContentsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
        return TextUtils.equals(oldItem.device.name, newItem.device.name)
    }
}

interface AdapterListener {

    fun onUnlockTask(unlockView: View, item: ScanResult)

}