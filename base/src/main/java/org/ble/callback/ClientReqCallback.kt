package org.ble.callback

import android.bluetooth.BluetoothDevice

/**
 * Author: yuzzha
 * Date: 2021-08-11 14:35
 * Description:
 * Remark:
 */
abstract class ClientReqCallback {

    open fun onClientResp(
        device: BluetoothDevice?,
        value: ByteArray?
    ) {

    }

    open fun onFail() {}

}