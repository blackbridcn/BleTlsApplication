package org.ble.callback

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic

/**
 * Author: yuzzha
 * Date: 2021-06-29 15:44
 * Description:
 * Remark:
 */
abstract class BleGattServerCallback {

    open fun onConnBleDevice(centerDevice: BluetoothDevice): Unit {

    }

    open fun onDisConnBleDevice(centerDevice: BluetoothDevice): Unit {

    }

    open fun onClientBleReq(
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        mtu:Int
    ): Unit {

    }

    open fun onDescriptorReadRequest(){

    }

    open fun onServiceStarted() {

    }

}