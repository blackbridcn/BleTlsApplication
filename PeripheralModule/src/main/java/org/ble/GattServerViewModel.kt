package org.ble


import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.os.Handler
import android.os.Looper
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.train.peripheral.R
import org.ble.callback.BleGattServerCallback
import org.ble.utils.BleCompat
import org.bouncycastle.tls.RecordFormat
import org.bouncycastle.tls.TlsFatalAlert
import org.bouncycastle.tls.TlsUtils
import org.e.ble.utils.HexStrUtils
import org.tls.peer.server.TlsServerUtils
import org.tls.protocol.ContentType
import org.tls.protocol.HandshakeType
import org.tls.protocol.TlsProtocolData
import org.tls.utils.ByteUtils
import org.tls.utils.BytesQueue
import org.utils.LogUtils
import java.io.IOException


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
            //LogUtils.e(TAG, "onClientBleReq : ${HexStrUtils.byteArrayToHexString(value)}")

            parseMsg(value)
           // readBleTlsData(value, device);

        }

        override fun onDisConnBleDevice(centerDevice: BluetoothDevice) {
            super.onDisConnBleDevice(centerDevice)
            hasConnCentral = false
            if (hasSetupGattServer) {
                stopTimeOutTask()
                // releaseGattServer()
                handler.post {
                    _errRes.value = R.string.ble_even_exception_tip
                }
            }
        }
    }


    //偏移量 ,fragment每条tls命令 除去header头数据后的，数据段长度 length数据位的偏移量
    val FRAGMENT_OFFSET = 5

    var packHeader = false

    private var packageCacheQueue = BytesQueue(0)

    var cacheHeader: ByteArray? = null

    var packSize: Int = 0
    var packCursor: Int = 0

    var lastType = byteArrayOf(
        HandshakeType.client_hello,
        HandshakeType.server_hello_done,
        HandshakeType.finished
    )

    fun parseMsg(msg: ByteArray) {



        var source = checkCacheHeader(msg)
        var length = source.size
        if (length > 0) {
            //是否已经接收到一包数据的头部数据(包括Tls长度部分)
            if (!packHeader) {
                //source 必须包括完整的 length（表示数据包长度的字节位）数据位，
                // TLV（Type-Length-Value）中至少要包含完整T（Type）V（Length）数据；
                if (length > FRAGMENT_OFFSET) {
                    packSize = TlsUtils.readUint16(source, RecordFormat.LENGTH_OFFSET) + RecordFormat.FRAGMENT_OFFSET
                    org.utlis.LogUtils.e(TAG, "----->> parseMsg  : ${HexStrUtils.byteArrayToHexString(msg)}")
                    org.utlis.LogUtils.e(TAG, "----->> parseMsg Handshake Type : ${HandshakeType.getName(source[RecordFormat.FRAGMENT_OFFSET])}")

                    packHeader = true
                    buildPackageValue(source, length)
                } else {
                    //不足Length数据位， 拼接到下一帧数据，继续
                    cacheHeader = ByteArray(length);
                    cacheHeader?.let { System.arraycopy(source, 0, it, 0, length) };
                }
            } else {
                buildPackageValue(source, length)
            }

        }
    }


    private fun buildPackageValue(source: ByteArray, length: Int) {

        if (packSize - packCursor > length) {
            //新的一帧数据中还是只是之前数据包中的一部分，还没有拼接完一包完整的数据包
            packCursor += length
            packageCacheQueue.addData(source, 0, length)
        } else if (packSize - packCursor == length) {
            //1.本次通讯中并不是最后一帧，而且其他包最后一帧刚好是接收到一帧数据
            //2.刚好是本次通讯中的最后一帧数据

            packageCacheQueue.addData(source, 0, length)

            var cacheLast = ByteArray(packSize)
            var size = packageCacheQueue.available()
            packageCacheQueue.read(cacheLast, size - packSize, packSize, 0)

            LogUtils.e(TAG, "read central Handshake Type : ${HandshakeType.getName(cacheLast[FRAGMENT_OFFSET])}")
            //
            if (cacheLast[0] == ContentType.handshake && lastType.contains(cacheLast[FRAGMENT_OFFSET])) {
                //最后一帧
                var buff = packageCacheQueue.availableByteArray()
                var serverHello = TlsServerUtils.offerInput(buff)

                if (serverHello != null) {
                    sendMsgToCentral(serverHello)
                }

            } else if(cacheLast[0] == ContentType.change_cipher_spec){
                //在接收到该协议后，所有接收到的数据都需要解密。
                initPackageState()
            } else {
                initPackageState()
            }

        } else {
            //新的一帧数据中，不仅包含之前的一包数据，而且还包含了下包数据中的部分数据
            var lastCount = packSize - packCursor
            packageCacheQueue.addData(source, 0, lastCount)

            initPackageState()

            //剩下的数据（ lastCount～ length ） 新的数据包数据，直接交给下一帧去处理，Length数据位， 拼接到下一帧数据，继续解析
            cacheHeader = ByteArray(length - lastCount);
            cacheHeader?.let { System.arraycopy(source, lastCount, it, 0, length - lastCount) };

        }

        LogUtils.e(TAG, "buildPackageValue : ${HexStrUtils.byteArrayToHexString(source)}")
    }

    private fun initPackageState() {
        packHeader = false
        packSize = 0
        packCursor = 0
    }

    private fun checkCacheHeader(source: ByteArray): ByteArray {
        var value = source

        cacheHeader?.let {
            // 将上次不足Length数据位的部分 字节数据， 拼接到下一帧数据，作为一个新一帧，继续处理
            var newValue = ByteArray(it.size + source.size)
            //拼接数据array
            System.arraycopy(it, 0, newValue, 0, it.size)
            System.arraycopy(value, 0, newValue, it.size, value.size)
            value = newValue
            cacheHeader = null
        }
        return value
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


    private fun sendMsgToCentral(respVo: ByteArray) {
        BleClient.getInstance().sendMsgToGattClient(respVo)
        LogUtils.e(TAG, "-------->> sendMsgToCentral :${HexStrUtils.byteArrayToHexString(respVo)}")
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