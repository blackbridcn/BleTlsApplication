package com.train.central.ui.home

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.train.central.databinding.FragmentHomeBinding
import org.ble.BleClient
import org.ble.callback.BleGattCallback
import org.ble.scan.*
import org.e.ble.utils.HexStrUtils
import org.recyclerview.IndexOutOfBoundsExcLinearLayoutManager
import org.utlis.LogUtils
import java.util.*

class HomeFragment : Fragment(), AdapterListener  {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val databind get() = _binding!!

    private val TAG = HomeFragment::javaClass.name

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = _binding.root

        val btnHome: Button = binding.btnHome
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = it
        })
        binding.btnHome.setOnClickListener {
           // BleClient.getInstance().startAdvertising()
        }
        return root
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}