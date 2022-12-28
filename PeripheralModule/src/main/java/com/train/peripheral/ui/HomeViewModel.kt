package com.train.peripheral.ui


import org.ble.GattServerViewModel
import org.ble.utils.HexStrUtils
import org.tls.peer.server.TlsServerUtils
import org.utils.LogUtils

class HomeViewModel : GattServerViewModel() {

    override fun onHandshakeComplete() {

    }


    fun sendMsg(){

        var  hexs="0A0B0C0D0E0F";

        var handshakeClose=TlsServerUtils.wrapData(HexStrUtils.hexToByteArray(hexs))

        LogUtils.e("-------------> onHandshakeComplete:${HexStrUtils.byteArrayToHexString(handshakeClose)}")
        handler.postDelayed({
             sendMsgToCentral(handshakeClose);
        },3000)
    }

}