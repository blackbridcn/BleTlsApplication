package org.ble.callback

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic

interface BleMsgCallback {

    fun receiverRemoteBleMsg(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic)

    fun sendMsgError(errorMsg: String?, errorRes: Int)
}