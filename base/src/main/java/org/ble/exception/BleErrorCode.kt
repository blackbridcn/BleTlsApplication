package org.ble.exception

/**
 * Author: yuzzha
 * Date: 2019/4/25 15:29
 * Description: ${DESCRIPTION}
 * Remark:
 */
interface BleErrorCode {
    companion object {
        const val CODE_PARAM_ERROR = 1 shl 31
        const val CONN_NO_FOUND_DEVICE_SERVICE = 1 shl 1
        const val CONN_SERVICE_STATE_ERROR = 1 shl 2

        const val CONN_WRITE_SERVICE_STATE_ERROR = 1 shl 6
        const val CONN_READ_MSG_ERROR = 1 shl 3

        const val CONN_NO_FOUND_DEVICE_UUID_SERVICE_ERROR = 1 shl 4

        const val CONN_DEVICE_CHARACTERISTIC_ERROR = 1 shl 5
    }
}