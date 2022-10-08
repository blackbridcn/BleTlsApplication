package org.ble.utils

import android.content.Context
import android.location.LocationManager

/**
 * Author: yuzzha
 * Date: 2021-06-17 14:55
 * Description:
 * Remark:
 */
object BleAssistUtils {
    /**
     * 判断位置定位是否打开
     *
     * @param context
     * @return
     */
    fun enableLocatService(context: Context): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}