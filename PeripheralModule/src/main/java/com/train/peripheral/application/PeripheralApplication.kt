package com.train.peripheral.application

import com.train.base.application.BaseApplication
import com.train.peripheral.contetant.Constants
import org.ble.BleClient
import org.ble.config.BleConfig

/**
 * Author: yuzzha
 * Date: 2021-12-09 12:10
 * Description:
 * Remark:
 */
class PeripheralApplication : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        initBle()
    }

    fun initBle() {
        var config = BleConfig.Builder()
            .rxCharacterUuid(Constants.BASE_RX_CHAR_UUID)
            .txCharacterUuid(Constants.BASE_TX_CHAR_UUID)
            .serviceUuid(Constants.BASE_SERVICE_UUID)
            .isPeripheral(true)
            .setScanTimeout(4500)
            .bleName("train")
            .build()
        BleClient.initBlutooth(this, config)
    }

}