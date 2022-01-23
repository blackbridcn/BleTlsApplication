package org.ble.callback

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic

/**
 * Author: yuzzha
 * Date: 2021-04-09 14:08
 * Description:
 * Remark:
 */
abstract class BleGattCallback {


    companion object {
        const val CONN_FAIL_DISCOVER_SERVICE = 0x01

        const val CONN_SERVICE_STATE_ERROR = 1 shl 2
    };

    /**
     * Ble 连接成功后回调
     *
     * @param gatt           BluetoothGatt
     * @param characteristic BluetoothGattCharacteristic
     */
    open fun onConnSuccess(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {}

    /**
     * Ble  正在连接
     */
    open fun onConningServer() {}

    open fun onConningFail(code: Int) {}


    open fun onBleServerResp(
         gatt:BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        mtu: Int
    ) {

    }

    /**
     * Ble 连接失败
     *
     * @param code BleErrorCode 失败Code
     * @param msg  连接失败ResId Msg
     * 备注：失败回调后，会自动断开连接并且清空所用的回调
     */
    open fun onConnFailure(code: Int, msg: Int) {}

    /**
     * Ble  断开连接
     * 备注：断开连接回调后，会自动断开连接并且清空所用的回调
     */
    open fun disConnDevice() {}

    /**
     * Ble连接成功 , 并且可以开始向Ble设备发消息
     *
     * @param gatt BluetoothGatt
     */
    abstract fun startSendMsg(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic)
}