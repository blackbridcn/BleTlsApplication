package com.train.peripheral.message

/**
 * Author: yuzzha
 * Date: 2021-06-16 10:03
 * Description:
 * Remark:
 */
object Constants {

    const val DUALITY_BASE_SERVICE_UUID = "fc630001-3445-4b20-8613-c3cb"

    const val DUALITY_BASE_RX_CHAR_UUID = "fc630002-3445-4b20-8613-c3cb"

    const val DUALITY_BASE_TX_CHAR_UUID = "fc630003-3445-4b20-8613-c3cb"

    // 客户端特性配置描述符(Client Characteristic Configuration Descriptor，CCCD)，
    // 这个描述符是给任何支持通知或指示功能的特性额外增加的。
    // 在CCCD中写入“1”使能通知功能，写入“2”使能指示功能，写入“0”同时禁止通知和指示功能
    const val DUALITY_CCCD_DESC_UUID = "00002902-0000-1000-8000-00805f9b34fb"

    val DUALITY_RELAY_BASE_UUID: ByteArray = byteArrayOf(
        0x17.toByte(), 0x0E.toByte(), 0xCE.toByte(), 0x89.toByte(),
        0xCB.toByte(), 0xC3.toByte(), 0x13.toByte(), 0x86.toByte(),
        0x20.toByte(), 0x4B.toByte(), 0x45.toByte(), 0x34.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x63.toByte(), 0xFC.toByte()
    )

    //披克BLE蓝牙设备 信号过滤阈值
    const val DUALITY_MIN_RSSI_VALUE = 0.45

    const val PACKAGE_PRE: Byte = 0x7E.toByte()

    const val PACKAGE_PRE_HEX = "7E"

    const val PACKAGE_SUB_0: Byte = 0xE7.toByte()
    const val PACKAGE_SUB_1: Byte = 0x58.toByte()
    const val PACKAGE_SUB_HEX = "E758"

}