package com.train.peripheral.ui

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.ble.GattServerViewModel

class HomeViewModel : GattServerViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text


    override fun startFunctionEvent(device: BluetoothDevice) {

    }
}