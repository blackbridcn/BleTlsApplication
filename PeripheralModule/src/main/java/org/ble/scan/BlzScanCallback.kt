package org.ble.scan

import android.bluetooth.BluetoothDevice
import com.hidglobal.duality.relay.contetant.Constants


/**
 * Author: yuzzha
 * Date: 2021-04-09 13:48
 * Description:
 * Remark:
 */
public abstract class BlzScanCallback {
    var scanTimeou: Long = -1
        protected set

    constructor() {
        scanTimeou = -1
    }

    constructor(scanTimeout: Long) {
        scanTimeou = scanTimeout
    }

    open fun onPreScan() {}

    open  fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray){}

    open fun onScanResult(callbackType: Int, result: ScanResult) {

    }

    open fun onBatchScanResults(results: List<ScanResult>) {

    }

    open fun checkRangeValid(rssi: Int): Boolean {
        if(rssi> Constants.BLE_RSSI_THRESHOLD_VALUE){
            return true
        }else{
            return false
        }


        /*  return if (isRangeFliter) {
              getFeature(rssi) <= Constants.DUALITY_MIN_RSSI_VALUE
          } else true*/
    }


    /**
     * 超时回调
     */
    open fun onScanTimeout() {}

    open fun onScanFailed(errorCode: Int) {

    }

    /**
     * 调用Stop中断
     */
    open fun onScanFinsh(){}

}