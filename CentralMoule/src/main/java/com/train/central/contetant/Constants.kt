package com.train.central.contetant

/**
 * Author: yuzzha
 * Date: 2021-12-09 12:13
 * Description:
 * Remark:
 */
object Constants {

    // 蓝牙信号强度过滤 阈值
    var BLE_RSSI_THRESHOLD_VALUE = -70

    const val BASE_SERVICE_UUID = "fc630001-3445-4b20-8613-c3cbffffffff"

    //Gatt Server中 发送数据的 BluetoothGattCharacteristic 的UUID
    //就是 Gatt 客户端中接受数据的　BluetoothGattCharacteristic 的UUID
    const val BASE_RX_CHAR_UUID = "fc630003-3445-4b20-8613-c3cbffffffff"
    //Gatt 中客户端 用来发送数据的  BluetoothGattCharacteristic 的UUID
    const val BASE_TX_CHAR_UUID = "fc630002-3445-4b20-8613-c3cbffffffff"

    const val BASE_CCCD_DESC_UUID = "00002902-0000-1000-8000-00805f9b34fb"
}