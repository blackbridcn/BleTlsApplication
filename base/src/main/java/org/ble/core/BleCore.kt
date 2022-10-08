package org.ble.core

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import org.ble.callback.BleGattCallback
import org.ble.callback.BleGattServerCallback
import org.ble.callback.BleMsgCallback
import org.ble.callback.ClientReqCallback
import org.ble.config.BleConfig
import org.ble.scan.BlzScanCallback
import java.util.*

/**
 * Author: yuzzha
 * Date: 2021-04-09 11:19
 * Description:
 * Remark:
 */
interface BleCore  {

    fun init(mContext: Context?, config: BleConfig?)

    fun startScan()

    fun startScan(scanTimeout: Long)

    fun startScan(serviceUuids: Array<UUID?>?)

    fun setScanCallback(callbacks: BlzScanCallback?)

    fun stopScan()

    fun connectGattServer(address: String?, bleGattCallback: BleGattCallback?)

    fun connectGattServer(
        device: BluetoothDevice?,
        autoConnect: Boolean,
        bleGattCallback: BleGattCallback?
    )

    val isConnect: Boolean
    val isEnabled: Boolean

    fun enableBluetooth(mActivity: Activity?, requestCode: Int): Boolean

    val isScan: Boolean

    fun sendMsgToGattServerDevice(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        msg: ByteArray
    )

    fun disConnGattServer()

    fun disConnGattServer(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic)


    /*----------------------------------------------------------------------------------*/

    fun supportAdvertisement(): Boolean

    fun startAdvertising(
        settings: AdvertiseSettings,
        advertiseData: AdvertiseData,
        callback: AdvertiseCallback
    )

    fun startAdvertising(serviceUUID: String, callback: AdvertiseCallback)

    fun startAdvertising(serviceUUID: ByteArray, callback: AdvertiseCallback)

    fun stopAdvertising(callback: AdvertiseCallback)

    fun setGattServerCallback(serverCallback: BleGattServerCallback)

    fun startGattServer(
        serviceUuid: String,
        rxCharUuid: String,
        txCharUuid: String,
        descUuid: String,
        serverCallback: BleGattServerCallback?
    )

    fun startGattServer(
        serviceUuid: String, rxCharUuid: String, txCharUuid: String, mCCCDUuid: String
    )


    fun sendMsgToGattClient(respVo: ByteArray)

    fun sendMsgToGattClient(respVo: ByteArray, clientReqCallback: ClientReqCallback)

    fun hasConnGattClient(): Boolean

    fun getClientAddress(): String?

    fun disConnGattClient(device: BluetoothDevice)

    fun closeGattServer()

    /*----------------------------------------------------------------------------------*/

}