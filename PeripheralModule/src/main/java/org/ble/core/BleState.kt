package org.ble.core

/**
 * Author: yuzzha
 * Date: 2019-07-15 19:11
 * Description: ${DESCRIPTION}
 * Remark:
 */
class BleState {

    interface ScanState {
        companion object {
            //正在扫描
            const val SCAN_STATE_DOING = 1 shl 1

            //扫描空闲状态
            const val SCAN_STATE_IDLE = 1 shl 2
        }
    }

    interface ConnState {
        companion object {
            //连接状态--->这里是指可以通信状态
            const val CONN_STATE = 1 shl 1

            //连接空闲状态
            const val CONN_STATE_IDLE = 1 shl 1

            //正在连接
            const val CONN_STATE_CONNECTING = 1 shl 2

            //已经连接
            const val CONN_STATE_CONNECTED = 1 shl 3
        }
    }
}