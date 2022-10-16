package com.train.central.application

import com.train.base.application.BaseApplication
import com.train.central.contetant.Constants
import org.ble.BleClient
import org.ble.config.BleConfig

class CentralApplication : BaseApplication(){

    override fun onCreate() {
        super.onCreate()
        initBle()
    }


    fun initBle() {
        val config = BleConfig.Builder()
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