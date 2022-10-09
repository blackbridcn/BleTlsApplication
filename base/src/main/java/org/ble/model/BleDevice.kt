package org.ble.model

import android.bluetooth.BluetoothDevice
import org.ble.utils.HexStrUtils

/**
 * Author: yuzzha
 * Date: 2021-04-09 14:06
 * Description:
 * Remark:
 */
class BleDevice : Comparable<BleDevice> {

    var relayId: String = ""
    var device: BluetoothDevice? = null
    var rssi = 0
    var rssiValue: String? = null
    var deviceName: String = ""

    var permission = false

    var open = false

    constructor(device: BluetoothDevice, rssi: Int, deviceId: String, permission: Boolean) {
        this.device = device
        this.rssi = rssi
        this.rssiValue = "RSSI:${rssi} dBm"
        this.open = permission
        this.relayId = deviceId
        device.name?.let {
            if (device.name.isBlank()) {
                this.deviceName = "Relay${deviceId}"
            } else {
                this.deviceName = device.name
            }
        }
    }

    constructor(device: BluetoothDevice, rssi: Int, deviceId: String, name: String ,local:String ) {
        this.device = device
        this.rssi = rssi
        this.rssiValue = local //"RSSI:${rssi} dBm"
        this.open = true
        this.relayId = deviceId
        this.deviceName=name
    }

    constructor(device: BluetoothDevice, rssi: Int, deviceId: String) {
        this.device = device
        this.rssi = rssi
        this.rssiValue = "RSSI:${rssi} dBm"
        this.relayId = deviceId
        device.name?.let {
            if (device.name.isBlank()) {
                this.deviceName = "Relay${deviceId}"
            } else {
                this.deviceName = device.name
            }
        }
    }

    constructor(device: BluetoothDevice, rssi: Int, deviceId: ByteArray) {
        this.device = device
        this.rssi = rssi
        this.rssiValue = "RSSI:${rssi} dBm"
        this.deviceName = HexStrUtils.byteArrayToHexString(deviceId) + "(iBeacon)"
    }

    constructor(device: BluetoothDevice,ibean: Boolean, rssi: Int) {
        this.device = device
        this.rssi = rssi
        this.rssiValue = "RSSI:${rssi} dBm"
        device.name?.let {
            this.deviceName ="${it}(IBean)"
        }
        device.name?:let {
            this.deviceName ="N/A(IBean) "
        }
    }
    constructor(device: BluetoothDevice, rssi: Int) {
        this.device = device
        this.rssi = rssi
        this.rssiValue = "RSSI:${rssi} dBm"
        device.name?.let {
            this.deviceName =it
        }
        device.name?:let {
            this.deviceName ="N/A"
        }
    }

    // e name, address, class, and bonding state
    override fun toString(): String {
        return "BleDeviceData(\n" +
                " device.name=${device!!.name}\n," +
                " device.address=${device!!.address}\n," +
                " device.type=${device!!.type}\n," +
                " rssi=$rssi \n"
    }

    override fun compareTo(other: BleDevice): Int {
        if (other == null) {
            return -1
        }
        if(this.open && !other.open){
            return -1
        }
        if(!this.open &&other.open){
            return 1
        }
        if (getFeature(this.rssi) == getFeature(other.rssi)) {
            return 0
        }
        return (getFeature(this.rssi) - getFeature(other.rssi)).toInt()
    }

    protected fun getFeature(rssi: Int): Double {
        return (Math.abs(rssi) - 59) / (10 * 2.0)
    }

}