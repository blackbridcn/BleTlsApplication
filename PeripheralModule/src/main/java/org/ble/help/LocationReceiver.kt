package org.ble.help

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.text.TextUtils
import androidx.annotation.NonNull
import com.hidglobal.duality.relay.application.BaseApplication


/**
 * Author: yuzzha
 * Date: 2021-09-02 17:16
 * Description:
 * Remark:
 */
class LocationReceiver constructor(val mContext: Context) :
    BroadcastReceiver(), LocationChangeListener {


    val GPS_ACTION = "android.location.PROVIDERS_CHANGED"

    companion object {
        val instance = SingletonLocationReceiver.holder
    }

    private object SingletonLocationReceiver {
        val holder = LocationReceiver(BaseApplication.getInstance()!!)
    }


    var list: MutableList<LocationChangeListener> = mutableListOf<LocationChangeListener>()

    init {
        val filter = IntentFilter()
        filter.addAction(GPS_ACTION)
        mContext.registerReceiver(this, filter)

    }

    fun addLocationChangeListener(
        @NonNull mContext: Context,
        @NonNull locationChangerListener: LocationChangeListener
    ) {
        if (!list.contains(locationChangerListener)) {
            list.add(locationChangerListener)
            //init state
            locationChangerListener.locationServiceState(gpsIsOpen(mContext))
        }
    }

    fun removeLocationChangeListener(@NonNull locationChangerListener: LocationChangeListener) {
        if (list.contains(locationChangerListener)) {
            list.remove(locationChangerListener)
        }
    }

    fun dispatchLocationChangeEven(locationServerState: Boolean) {
        list.forEach {
            it.locationServiceState(locationServerState)
        }
    }

    /**
     * 释放资源
     */
    fun onDestroy() {
        mContext.unregisterReceiver(this)
    }

    var locationManager: LocationManager? = null

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    fun gpsIsOpen(context: Context): Boolean {
        locationManager ?: let {
            locationManager =
                context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        locationManager?.let {
            // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
            val gps = it.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
            val network = it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            return if (gps || network) {
                true
            } else false
        }
        return false
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (TextUtils.equals(GPS_ACTION, intent.action)) {
            locationServiceState(gpsIsOpen(context))
        }
    }

    override fun locationServiceState(locationServerState: Boolean) {
        dispatchLocationChangeEven(locationServerState)
    }
}