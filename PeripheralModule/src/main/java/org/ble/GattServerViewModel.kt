package org.ble

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.hidglobal.ui.ble.conn.enums.BleCmdEnum
import com.train.base.application.BaseApplication
import com.train.peripheral.R
import com.train.peripheral.message.BleEncryptType

import org.ble.BleClient
import org.ble.callback.BleGattServerCallback

import org.ble.utils.BleCompat

import org.bouncycastle.tls.ByteQueue

import org.e.ble.utils.HexStrUtils


import org.utils.LogUtils


/**
 * Author: yuzzha
 * Date: 2021-09-01 15:25
 * Description:
 * Remark:  Periphery
 */
abstract class GattServerViewModel : ViewModel() {

    val TAG: String = GattServerViewModel::javaClass.name

    var handler = Handler(Looper.getMainLooper())

    var _errMsg: MutableLiveData<String> = MutableLiveData<String>()
    var errMsg: LiveData<String> = _errMsg

    var _errRes: MutableLiveData<Int> = MutableLiveData<Int>()
    var errRes: LiveData<Int> = _errRes


    var serviceUuid: String? = null

    var bleHandShake = false

    var hasSetupGattServer = false

    var hasAction = false

    var tlsFinished = false

    var device: BluetoothDevice? = null

    val timeOut: Long = 4 * 1000


    private val timeOutRunnable = Runnable {
        releaseGattServer()
        if (hasConnCentral) {
            _errRes.value = R.string.ble_even_build_conn_not_msg_timeout_tip
        } else {
            _errRes.value = R.string.ble_even_no_conn_timeout_tip
        }
    }

    fun startGattServer(
        serviceUuid: String,
        rxCharUuid: String,
        txCharUuid: String,
        mCCCDUuid: String
    ) {
        this.serviceUuid = serviceUuid
        hasSetupGattServer = true
        BleClient.getInstance().startGattServer(
            serviceUuid,
            rxCharUuid,
            txCharUuid,
            mCCCDUuid,
            mGattServerCallback
        )
    }

    var hasConnCentral: Boolean = false

    var mGattServerCallback = object : BleGattServerCallback() {

        override fun onServiceStarted() {
            super.onServiceStarted()
            BleClient.getInstance().startAdvertising(
                serviceUuid,
                advertiseCallback
            )
        }

        override fun onConnBleDevice(centerDevice: BluetoothDevice) {
            super.onConnBleDevice(centerDevice)
            hasConnCentral = true
        }

        override fun onClientBleReq(
            device: BluetoothDevice,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            mtu: Int,
        ) {
            super.onClientBleReq(device, characteristic, value, mtu)
            setCurrentDevice(device)
            //TLS消息
            //readBleTlsData(value, device)
        }

        override fun onDisConnBleDevice(centerDevice: BluetoothDevice) {
            super.onDisConnBleDevice(centerDevice)
            hasConnCentral = false
            if (hasSetupGattServer) {
                stopTimeOutTask()
                releaseGattServer()
                handler.post {
                    _errRes.value = R.string.ble_even_exception_tip
                }
            }
        }
    }

    var temp: MutableList<Byte>? = null
    var packSize = 0

    var fragmentLen = 0

    var list = ArrayList<ByteArray>()
    var lastHead: ByteArray? = null

    val LENGTH_OFFSET = 3
    val FRAGMENT_OFFSET = 5
    var hasHead = true

    private var tlsHandshakeQueue = ByteQueue(0)


    fun readTlsPackage(source: ByteArray, index: Int, bodyLen: Int) {
        if (tlsHandshakeQueue.available() == 0) {
            var dest = ByteArray(bodyLen)
            System.arraycopy(source, index, dest, 0, bodyLen)

            LogUtils.e(TAG, "${HexStrUtils.byteArrayToHexString(dest)}")

        }
        tlsHandshakeQueue.addData(source, index, bodyLen)
    }

    fun setCurrentDevice(device: BluetoothDevice) {
        this.device = device
    }


    fun dispatchBleFailEven(@StringRes fail: Int) {
        handler.post {
            _errRes.value = fail
        }
        releaseGattServer()
    }


    abstract fun startFunctionEvent(device: BluetoothDevice)


    open fun stopScanTask() {

    }

    override fun onCleared() {
        super.onCleared()
        try {
            //蓝牙状态广播
           // BaseApplication.getInstance()?.unregisterReceiver(bluetoothStateBroadcastReceiver)
            if (BleCompat.isMarshmallowOrAbove()) {
                //定位功能开关  Android 6.0 Marshmallow overview.
              //  LocationReceiver.instance.removeLocationChangeListener(locationChangerListener)
            }
        } catch (e: IllegalArgumentException) {

        }
    }


    fun sendMsgToCentral(respVo: ByteArray) {

        BleClient.getInstance().sendMsgToGattClient(respVo)
        //LogUtils.e(TAG, "sendMsgToRelay  HEX:" + HexByteUtils.bytesToHexString(respVo))
    }

    var advertiseCallback = object : AdvertiseCallback() {

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            var logErrMsg = ""
            when (errorCode) {
                ADVERTISE_FAILED_ALREADY_STARTED -> {
                    logErrMsg = "onStartFailure advertising is already started "
                }
                ADVERTISE_FAILED_DATA_TOO_LARGE -> {
                    logErrMsg = "advertise data to be broadcasted is larger than 31 bytes "
                }
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> {
                    logErrMsg =
                        "advertise Failed to start advertising because no advertising instance is available "
                }
                ADVERTISE_FAILED_INTERNAL_ERROR -> {
                    logErrMsg =
                        "advertise Failed to start , Operation failed due to an internal error"
                }
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> {
                    logErrMsg =
                        "advertise Failed to start , This feature is not supported on this platform"
                }
            }
            LogUtils.e(
                TAG,
                " AdvertiseCallback onStartFailure \n errorCode : ${errorCode} \n logErrMsg :${logErrMsg}"
            )
            if (ADVERTISE_FAILED_ALREADY_STARTED != errorCode && hasSetupGattServer) {
                stopTimeOutTask()
                releaseGattServer()
                handler.post {
                    _errMsg.value = logErrMsg //R.string.module_relay_ble_even_exception_tip
                }

            }
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)

            LogUtils.e(TAG, "onStartSuccess ${settingsInEffect}")
        }
    }

    fun stopAdvertise() {
        /*hasAdv = false*/
        BleClient.getInstance().stopAdvertising(advertiseCallback)
    }

    open fun releaseGattServer() {
        bleHandShake = false
        tlsFinished = false
        hasSetupGattServer = false
        hasAction = false

        stopAdvertise()
        BleClient.getInstance().closeGattServer()
    }

    fun startTimeOutTask() {
        handler.removeCallbacks(timeOutRunnable)
        handler.postDelayed(timeOutRunnable, timeOut)
    }

    fun stopTimeOutTask() {
        handler.removeCallbacks(timeOutRunnable)
    }

    fun isResetBle() {
        BleClient.getInstance().doResetBleTask()
    }

    fun isEnabled(): Boolean {
        return BleClient.getInstance().canBlz()
    }



}