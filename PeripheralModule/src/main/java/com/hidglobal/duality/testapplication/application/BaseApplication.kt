package com.hidglobal.duality.relay.application

import android.app.Application
import android.content.Context
import com.hidglobal.duality.relay.contetant.Constants
import org.ble.BleClient
import org.ble.config.BleConfig

/**
 * Author: yuzzha
 * Date: 2021-12-09 12:10
 * Description:
 * Remark:
 */
class BaseApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        context = this
    }

    override fun onCreate() {
        super.onCreate()
        initBle()
    }

    fun initBle() {
        var config = BleConfig.Builder()
            .rxCharacterUuid(Constants.DUALITY_BASE_RX_CHAR_UUID)
            .txCharacterUuid(Constants.DUALITY_BASE_TX_CHAR_UUID)
            .serviceUuid(Constants.DUALITY_BASE_SERVICE_UUID)
            .isPeripheral(true)
            .setScanTimeout(4500)
            .bleName("d-relay")
            .build()
        BleClient.initBlutooth(this, config)
    }

    companion object{
        @JvmStatic
        var context: BaseApplication? = null
            private set

        fun getInstance(): BaseApplication? {
            return context
        }
    }
}