package org.ble.core.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import org.ble.callback.BleGattServerCallback
import org.ble.callback.ClientReqCallback

/**
 * Author: yuzzha
 * Date: 2021-10-14 13:56
 * Description:
 * Remark:
 */
 interface PeripheralCore {


    fun startAdvertising(
        settings: AdvertiseSettings,
        advertiseData: AdvertiseData,
        callback: AdvertiseCallback
    )

    fun startAdvertising(serviceUUID: String, callback: AdvertiseCallback)

    fun startAdvertising(serviceUUID: ByteArray, callback: AdvertiseCallback)

    fun stopAdvertising(callback: AdvertiseCallback)

    fun startGattServer(
        serviceUuid: String,
        rxCharUuid: String,
        txCharUuid: String,
        descUuid: String
    )

    fun startGattServer(
        serviceUuid: String,
        rxCharUuid: String,
        txCharUuid: String,
        descUuid: String,
        serverCallback: BleGattServerCallback?
    )

    fun setGattServerCallback(serverCallback: BleGattServerCallback)

    fun sendMsgToGattClient(respVo: ByteArray)

    fun sendMsgToGattClient(respVo: ByteArray, clientReqCallback: ClientReqCallback)

    fun hasConnGattClient(): Boolean

    fun getClientAddress(): String?

    fun disConnGattClient(device: BluetoothDevice)

    fun closeGattServer()


 }