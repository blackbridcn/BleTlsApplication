package com.train.peripheral.ui

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.train.peripheral.contetant.Constants

import com.train.peripheral.databinding.FragmentFirstBinding
import org.ble.scan.*

import org.utils.LogUtils
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private val TAG = HomeFragment::javaClass.name

    lateinit var homeViewModel: HomeViewModel

    private var _binding: FragmentFirstBinding? = null

    private val databind get() = _binding!!


    val INDEX_ID = "ffffffff"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return databind.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        databind.btnValue.setOnClickListener {
            homeViewModel.startGattServer(
                Constants.BASE_SERVICE_UUID,
                Constants.BASE_RX_CHAR_UUID,
                Constants.BASE_TX_CHAR_UUID ,
                Constants.BASE_CCCD_DESC_UUID
            )
        }

        //  var ch = TlsClientUtils.startHello();
        LogUtils.e("TAG", "----------------_> ")
        //  LogUtils.e(TAG, ByteHexUtils.byteArrayToHexString(ch));
    }


    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    var hasHead = false

    // var tlsPackageData: TlsPackageData? = null

    var temp: MutableList<Byte>? = null

    var packLength = 0

    var raw: ByteArray? = null


    open fun handlerResp(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        resp: ByteArray
    ) {
        //  var serverHello = TlsServerUtils.receiverClientHello(resp)
        //   BleClient.getInstance().sendMsgToGattServerDevice(gatt, characteristic, serverHello)
    }
}


