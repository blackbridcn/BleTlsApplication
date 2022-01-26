package com.hidglobal.duality.testapplication.ui

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.content.ReceiverCallNotAllowedException
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.hidglobal.duality.testapplication.R
import com.hidglobal.duality.testapplication.databinding.FragmentFirstBinding
import com.hidglobal.duality.testapplication.message.BleDataUtils
import com.hidglobal.duality.testapplication.mvi.SampleMviActivity
import org.ble.BleClient
import org.ble.callback.BleGattCallback
import org.ble.core.central.CentralImpl
import org.ble.scan.*
import org.e.ble.utils.HexStrUtils
import org.recyclerview.IndexOutOfBoundsExcLinearLayoutManager
import org.tls12.TlsServerUtils

import org.utils.LogUtils
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), AdapterListener {

    private val TAG = FirstFragment::javaClass.name

    private var _binding: FragmentFirstBinding? = null

    private val databind get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return databind.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databind.buttonFirst.setOnClickListener {
            //startActivity(Intent(requireContext(),SampleMviActivity::class.java))
           findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        initWidget()
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
        startScanTask()
    }

    override fun onPause() {
        super.onPause()
        stopScanTask()
    }

    var tlsClientDeviceName = "duality"

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

        LogUtils.e(TAG,"------------>  startScanTask :"+scanner.javaClass.simpleName)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            LogUtils.e(TAG, "--------------------> onConnSuccess :")
        }

        override fun startSendMsg(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            LogUtils.e(TAG, "--------------------> startSendMsg :")

            //blzGattCallback
            //7E1A08000801431911122106CD2A

            BleClient.getInstance().sendMsgToGattServerDevice(
                gatt, characteristic,
                HexStrUtils.hexToByteArray("7E1A08000801431911122106CD2A")
            )
        }

        override fun onBleServerResp(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            mtu: Int
        ) {
            super.onBleServerResp(gatt, characteristic, value, mtu)
            LogUtils.e(
                TAG,
                "--------------------> onBleServerResp :" + HexStrUtils.byteArrayToHexString(value)
            )
         //   receiveBuildPackage(gatt, characteristic, value)
            BleClient.getInstance().sendMsgToGattServerDevice(
                gatt, characteristic,
                BleDataUtils.buildUnlockMsg(Date())
            )
            ;
        }
    }

    var hasHead = false

   // var tlsPackageData: TlsPackageData? = null

    var temp: MutableList<Byte>? = null

    var packLength = 0

    var raw: ByteArray? = null

  /*  fun receiveBuildPackage(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        LogUtils.e(
            TAG, "value : ${value.size} }"
        )
        if (value.size > 0) {

            LogUtils.e(
                TAG, "  ${hasHead} }"
            )

            if (!hasHead) {
                tlsPackageData = TlsPackageData.initPackage(value)

                if (temp != null) temp!!.clear() else temp = ArrayList<Byte>()

                packLength = tlsPackageData?.contentLength!! + 5

                for (i in 0..value.size - 1) {
                    temp?.add(value[i])
                }
                hasHead = true
            } else {
                if ((value.size + temp?.size!!) <= packLength) {
                    for (i in 0..value.size - 1) {
                        temp?.add(value[i])
                    }
                }
                LogUtils.e(TAG, " temp size : ${temp?.size.toString()} }")
                if (temp?.size!! >= packLength) {
                    var byteArray = ByteArray(temp!!.size)
                    for (i in temp!!.indices) {
                        byteArray[i] = temp!!.get(i)
                    }
                    temp?.clear()
                    hasHead = false
                    handlerResp(gatt, characteristic, byteArray)
                }
            }
        } else {

        }
    }*/

    open fun handlerResp(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        resp: ByteArray
    ) {
        var serverHello = TlsServerUtils.receiverClientHello(resp)
        BleClient.getInstance().sendMsgToGattServerDevice(gatt, characteristic, serverHello)
    }
}


