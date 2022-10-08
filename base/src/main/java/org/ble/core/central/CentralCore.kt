package org.ble.core.central

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import org.ble.callback.BleGattCallback
import org.ble.callback.BleMsgCallback

/**
 * Author: yuzzha
 * Date: 2021-10-14 13:57
 * Description:
 * Remark:
 */
 interface CentralCore {

    fun connectGattServer(address: String?, bleGattCallback: BleGattCallback?)

    fun connectGattServer(
        device: BluetoothDevice?,
        autoConnect: Boolean,
        bleGattCallback: BleGattCallback?
    )


    fun sendMsgToGattServerDevice(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        msg: ByteArray
    )

}