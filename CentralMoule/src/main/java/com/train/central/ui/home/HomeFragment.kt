package com.train.central.ui.home

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.train.central.databinding.ModuleCentralMainHomeFragmentBinding

import org.ble.BleClient
import org.ble.callback.BleGattCallback
import org.ble.scan.*
import org.ble.utils.HexStrUtils
import org.bouncycastle.tls.RecordFormat
import org.bouncycastle.tls.TlsFatalAlert
import org.bouncycastle.tls.TlsUtils
import org.recyclerview.IndexOutOfBoundsExcLinearLayoutManager
import org.tls.peer.client.TlsClientUtils
import org.tls.peer.server.TlsServerUtils
import org.tls.protocol.ContentType
import org.tls.protocol.HandshakeType
import org.tls.protocol.TlsProtocolData
import org.tls.utils.ByteUtils
import org.tls.utils.BytesQueue
import org.utlis.LogUtils
import java.io.IOException
import java.util.*

class HomeFragment : Fragment(), AdapterListener {

    private lateinit var homeViewModel: HomeViewModel

    lateinit var databind: ModuleCentralMainHomeFragmentBinding

    private val TAG = HomeFragment::javaClass.name

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        databind = ModuleCentralMainHomeFragmentBinding.inflate(inflater)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = it
        })
        initWidget()
        return databind.root
    }


    lateinit var listAdapter: ScanAdapter

    fun initWidget() {
        listAdapter = ScanAdapter(this)
        databind.rvScan.layoutManager = IndexOutOfBoundsExcLinearLayoutManager(context)
        databind.rvScan.adapter = listAdapter
        databind.rvScan.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun onResume() {
        super.onResume()
        scanDevice.clear()
        scanCache.clear()
        startScanTask()
    }

    override fun onPause() {
        super.onPause()
        stopScanTask()
    }

    var tlsClientDeviceName = "train"

    private fun startScanTask() {

        var scanner: BluetoothScannerProvider = BluetoothScannerProvider.getScanner()

        val filter = ScanFilter.Builder()
            .setDeviceName(tlsClientDeviceName)
            .build()

        val filters: MutableList<ScanFilter> = ArrayList(1)
        filters.add(filter)

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setMatchLostDeviceTimeout(3000)
            .setUseHardwareBatchingIfSupported(false)
            .build()

        LogUtils.e(TAG, "------------>  startScanTask :" + scanner.javaClass.simpleName)
        scanner.startScan(filters, settings, scanCallback)
    }


    var scanDevice: ArrayList<ScanResult> = arrayListOf();
    var scanCache: ArrayMap<String, ScanResult> = arrayMapOf();

    var scanCallback: BlzScanCallback = object : BlzScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            scanCache.get(result.device.name) ?: let {
                scanCache.put(result.device.name, result)
                scanDevice.add(result)
                listAdapter.submitList(scanDevice)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            //异常处理
            LogUtils.e(TAG, "onScanFailed :${errorCode}")
        }

        override fun onBatchScanResults(results: List<ScanResult>) {

        }
    }

    private fun stopScanTask() {
        var scanner: BluetoothScannerProvider = BluetoothScannerProvider.getScanner()
        scanner.stopScan(scanCallback)
    }


    override fun onUnlockTask(unlockView: View, item: ScanResult) {
        var scanner: BluetoothScannerProvider = BluetoothScannerProvider.getScanner()
        scanner.stopScan(scanCallback)
        BleClient.getInstance().connectGattServer(item.device, blzGattCallback)
    }

    var blzGattCallback = object : BleGattCallback() {

        override fun onConningServer() {
            super.onConningServer()
            LogUtils.e(TAG, "--------------------> onConningServer :")
        }

        override fun onConnSuccess(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onConnSuccess(gatt, characteristic)

        }

        override fun startSendMsg(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            //blzGattCallback
            var startHakes = TlsClientUtils.startHandshake()
            BleClient.getInstance().sendMsgToGattServerDevice(gatt, characteristic, startHakes)

            var protocolData = TlsProtocolData.initData(startHakes);
            LogUtils.e("-----------> startHakes :${protocolData}")

        }

        override fun onBleServerResp(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            mtu: Int
        ) {
            super.onBleServerResp(gatt, characteristic, value, mtu)
            parseMsg(value, gatt, characteristic);
            //readBleTlsData(value,gatt,characteristic);
            //LogUtils.e(TAG, "onBleServerResp ： ${ org.e.ble.utils.HexStrUtils.byteArrayToHexString(value)}")

            //   receiveBuildPackage(gatt, characteristic, value)

        }
    }


    var lastHead: ByteArray? = null

    val LENGTH_OFFSET = 3

    //偏移量 ,fragment每条tls命令 除去header头数据后的，数据段长度 length数据位的偏移量
    val FRAGMENT_OFFSET = 5

    var hasHead = true

    var hasFragment = false


/*    private var tlsHandshakeQueue = BytesQueue(0)
    private var tlsCacheQueue = BytesQueue(0)
    var packSize = 0

    fun readBleTlsData(value: ByteArray,  gatt: BluetoothGatt,
                       characteristic: BluetoothGattCharacteristic,) {
        var length = value.size
        if (length > 0) {
            var values = value
            //
            lastHead?.let {
                length += it.size

                var newValue = ByteArray(length)
                System.arraycopy(lastHead, 0, newValue, 0, it.size)
                System.arraycopy(values, 0, newValue, it.size, values.size)
                values = newValue
                lastHead = null
            }

            var index = 0
            //
            while (index <= length && (length - index > 0) && hasHead) {

                if (length - index >= FRAGMENT_OFFSET) {

                    var bodyLen = ByteUtils.readUint16(values, index + LENGTH_OFFSET) + FRAGMENT_OFFSET
                    if (index + bodyLen <= length) {
                        //
                        readTlsPackage(values, index, bodyLen,gatt,characteristic)

                        index += bodyLen
                    } else {
                        hasHead = false
                        lastHead = ByteArray(length - index)
                        System.arraycopy(values, index, lastHead, 0, length - index);
                    }
                } else {
                    hasHead = false
                    lastHead = ByteArray(length - index)
                    System.arraycopy(values, index, lastHead, 0, length - index);
                }
            }
            hasHead = true
            lastHead ?: let {
                try {
                    tlsHandshakeQueue.removeData(tlsHandshakeQueue.available())
                   // tlsFinished = true
                   // startFunctionEvent(device)
                    /// var clientKeyExchange = TlsClientUtils.continueHandshake(handshakeQueue)
                    // sendMsgToCentral(clientKeyExchange)
                } catch (e: IOException) {
                    if (e is TlsFatalAlert) {
                        var alert = Integer.toHexString(e.alertDescription.toInt())
                        while (alert.length < 4) {
                            alert = "0${alert}"
                        }
                        //var tlsFail = BleDataUtils.buildTlsErrCommand(alert)
                        //  startTimeOutTask()
                        //  sendMsgToCentral(tlsFail)
                    } else {
                        //var tlsFail = BleDataUtils.buildTlsErrCommand("0015")
                        //startTimeOutTask()
                        // sendMsgToCentral(tlsFail)
                    }
                    //releaseGattServer()
                    //_errMsg.value = e.message
                }
            }
        }
    }


    var cache=ArrayList<ByteArray>();

    fun readTlsPackage(source: ByteArray, index: Int, bodyLen: Int, gatt: BluetoothGatt,
                       characteristic: BluetoothGattCharacteristic,) {
        if (tlsHandshakeQueue.available() == 0) {
            var dest = ByteArray(bodyLen)
            System.arraycopy(source, index, dest, 0, bodyLen)

            var protocolData = TlsProtocolData.initData(dest);
            LogUtils.e("-----------> readTlsPackage :${protocolData}")


            var startHakes = TlsClientUtils.offerInput(dest)


            if(startHakes!=null){

                var protocolData = TlsProtocolData.initData(dest);
                LogUtils.e("-----------> readTlsPackage :${protocolData}")

                BleClient.getInstance().sendMsgToGattServerDevice(gatt, characteristic, startHakes)
            }


            LogUtils.e(TAG, "${org.e.ble.utils.HexStrUtils.byteArrayToHexString(dest)}")



        }
        tlsHandshakeQueue.addData(source, index, bodyLen)
    }*/

    //processCharacteristicChanged

    //16030400B5010000B10303525DDE805DCC46F879484C8D4D2B1137C78765CFC013A217B4094D8887854AB82028E4533F2536F237B71D48DFEFD9E82C0726BE927F216F207038FC99118B4ED50006C0AC130100FF010000620017000000160000002B00050403040303000A000400020016003300020000000500050100000000000D0030002E080708080403050306030804080508060809080A080B040105010601040205020602030303010302020302010202000B00020100
    //160303003F0200003B0303F2D6AAC7868CDCE5A652FD764ABE7DB94D1DEE036AA5CE588D0C80C3B20B440100C0AC0000130005000000170000FF01000100000B00020100


    var packHeader = false

    private var tlsCacheQueue = BytesQueue(0)
    private var packageCacheQueue = BytesQueue(0)

    var cacheArray: ByteArray? = null
    var cacheHeader: ByteArray? = null

    var packSize: Int = 0
    var packCursor: Int = 0

    var lastType = byteArrayOf(
        HandshakeType.client_hello,
        HandshakeType.server_hello_done,
        HandshakeType.finished
    )

    fun parseMsg(
        msg: ByteArray, gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
    ) {

        LogUtils.e(TAG, "------------->> parseMsg : ${org.e.ble.utils.HexStrUtils.byteArrayToHexString(msg)}")
        var source = checkCacheHeader(msg)
        var length = source.size
        if (length > 0) {
            //是否已经接收到一包数据的头部数据(包括Tls长度部分)
            if (!packHeader) {
                //source 必须包括完整的 length（表示数据包长度的字节位）数据位，
                // TLV（Type-Length-Value）中至少要包含完整T（Type）V（Length）数据；
                if (length > FRAGMENT_OFFSET) {
                    packSize = TlsUtils.readUint16(source, RecordFormat.LENGTH_OFFSET
                    ) + RecordFormat.FRAGMENT_OFFSET
                    packHeader = true
                    buildPackageValue(source, length, gatt, characteristic)
                } else {
                    //不足Length数据位， 拼接到下一帧数据，继续
                    cacheHeader = ByteArray(length);
                    cacheHeader?.let { System.arraycopy(source, 0, it, 0, length) };
                }
            } else {
                buildPackageValue(source, length, gatt, characteristic)
            }

        }

    }




    private fun buildPackageValue(
        source: ByteArray, length: Int, gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
    ) {
        if (packSize - packCursor > length) {
            //新的一帧数据中还是只是之前数据包中的一部分，还没有拼接完一包完整的数据包
            packCursor += length
            packageCacheQueue.addData(source, 0, length)
        } else if (packSize - packCursor == length) {
            //1.本次通讯中并不是最后一帧，而且其他包最后一帧刚好是接收到一帧数据
            //2.刚好是本次通讯中的最后一帧数据

            //解决:
            packageCacheQueue.addData(source, 0, length)

            LogUtils.e(TAG, "------------->> buildPackageValue : ")

            var cacheLast = ByteArray(packSize);
            var size = packageCacheQueue.available();
            packageCacheQueue.read(cacheLast, size - packSize, packSize, 0);

            LogUtils.e(TAG, "${org.e.ble.utils.HexStrUtils.byteArrayToHexString(cacheLast)}")

            if (cacheLast[0] == ContentType.handshake && lastType.contains(cacheLast[FRAGMENT_OFFSET])) {
                //最后一帧
                var buff = packageCacheQueue.availableByteArray();


                var startHakes = TlsClientUtils.offerInput(buff)
                if (startHakes != null) {
                    BleClient.getInstance()
                        .sendMsgToGattServerDevice(gatt, characteristic, startHakes)

                    //var protocolData = TlsProtocolData.initData(serverHello);
                    // org.utlis.LogUtils.e("-----------> serverHello :${protocolData}")
                }

            }
            initPackageState();


        } else {
            //新的一帧数据中，不仅包含之前的一包数据，而且还包含了下包数据中的部分数据
            var lastCount = packSize - packCursor
            packageCacheQueue.addData(source, 0, lastCount)

            LogUtils.e(TAG, "------------->> buildPackageValue lastCount: ")
            initPackageState()

            //剩下的数据（ lastCount～ length ） 新的数据包数据，直接交给下一帧去处理，Length数据位， 拼接到下一帧数据，继续解析
            cacheHeader = ByteArray(length - lastCount);
            cacheHeader?.let { System.arraycopy(source, lastCount, it, 0, length - lastCount) };

        }
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

}