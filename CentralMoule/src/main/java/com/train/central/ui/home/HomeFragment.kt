package com.train.central.ui.home

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.core.widget.addTextChangedListener
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
import org.bouncycastle.tls.TlsUtils
import org.recyclerview.IndexOutOfBoundsExcLinearLayoutManager
import org.tls.peer.client.TlsClientUtils
import org.tls.protocol.ContentType
import org.tls.protocol.HandshakeType
import org.tls.utils.BytesQueue
import org.utlis.LogUtils
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
     var editText=  EditText(requireContext())
        editText.addTextChangedListener {  }
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
        }

        override fun onBleServerResp(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            mtu: Int
        ) {
            super.onBleServerResp(gatt, characteristic, value, mtu)
            parseMsg(value, gatt, characteristic)
        }
    }


    //偏移量 ,fragment每条tls命令 除去header头数据后的，数据段长度 length数据位的偏移量
    val FRAGMENT_OFFSET = 5

    //    17 0303 00 1E 0000000000000001036CC611F283FF
    var packHeader = false

    private var packageCacheQueue = BytesQueue(0)

    var cacheHeader: ByteArray? = null

    var packSize: Int = 0
    var packCursor: Int = 0

    var lastType = byteArrayOf(
        HandshakeType.hello_request,
        HandshakeType.client_hello,
        HandshakeType.server_hello_done,
        HandshakeType.finished,
    )

    fun parseMsg(
        msg: ByteArray, gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
    ) {

        var source = checkCacheHeader(msg)
        var length = source.size
        if (length > 0) {
            //是否已经接收到一包数据的头部数据(包括Tls长度部分)
            if (!packHeader) {
                //source 必须包括完整的 length（表示数据包长度的字节位）数据位，
                // TLV（Type-Length-Value）中至少要包含完整T（Type）V（Length）数据；
                if (length > FRAGMENT_OFFSET) {
                    packSize = TlsUtils.readUint16(
                        source,
                        RecordFormat.LENGTH_OFFSET
                    ) + RecordFormat.FRAGMENT_OFFSET
                    LogUtils.e(
                        TAG,
                        "----->> parseMsg Handshake Type : ${HandshakeType.getName(source[RecordFormat.FRAGMENT_OFFSET])}"
                    )
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

            //解决:枚举
            packageCacheQueue.addData(source, 0, length)

            var cacheLast = ByteArray(packSize);
            var size = packageCacheQueue.available();

            packageCacheQueue.read(cacheLast, 0, packSize, size - packSize);

            LogUtils.e("----------->tls offerInput :${ContentType.getName(cacheLast[0])}")

            if (cacheLast[0] == ContentType.handshake && lastType.contains(cacheLast[FRAGMENT_OFFSET])) {
                //最后一帧
                var buff = packageCacheQueue.availableByteArray();
                LogUtils.e("----------->tls offerInput :${HexStrUtils.byteArrayToHexString(buff)}")
                packageCacheQueue.clear();
                var startHakes = TlsClientUtils.offerInput(buff)
                if (startHakes != null) {
                    BleClient.getInstance()
                        .sendMsgToGattServerDevice(gatt, characteristic, startHakes)
                }
            } else if (cacheLast[0] == ContentType.application_data) {
                var buff = packageCacheQueue.availableByteArray();
                var unWrap = TlsClientUtils.unWrapData(buff)

                LogUtils.e(
                    "----------->TlsClientUtils unWrapData :${
                        HexStrUtils.byteArrayToHexString(
                            unWrap
                        )
                    }"
                )

            }
            initPackageState();

        } else {
            //新的一帧数据中，不仅包含之前的一包数据，而且还包含了下包数据中的部分数据
            var lastCount = packSize - packCursor
            packageCacheQueue.addData(source, 0, lastCount)

            LogUtils.e(TAG, "------------->> buildPackageValue lastCount: ")
            initPackageState()

            if (length - lastCount >= RecordFormat.FRAGMENT_OFFSET) {
                // 如果剩下数据长度超过了 TLV中L所需的长度就必须需要直接递归处理，
                // 因为如果最后这个包数据刚好是V为空（TLV中的Value）的数据包package，
                // 即本次RTT的最后通知类数据包再没有数据传输，不递归则永远不会触发对这包数据处理了，不会触发下一步动作了
                var lastPack = ByteArray(length - lastCount);
                System.arraycopy(source, lastCount, lastPack, 0, length - lastCount)
                parseMsg(lastPack, gatt, characteristic)

            } else {
                //剩下的数据（ lastCount～ length ） 新的数据包数据，直接交给下一帧去处理，Length数据位， 拼接到下一帧数据，继续解析
                cacheHeader = ByteArray(length - lastCount);
                System.arraycopy(source, lastCount, cacheHeader, 0, length - lastCount)
            }
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

    var complete = false;


}