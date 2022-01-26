package com.hidglobal.ui.ble.conn.enums

/**
 * Author: yuzzha
 * Date: 2021-06-18 17:43
 * Description:
 * Remark:
 */
enum class BleCmdEnum(var code: Byte, var desc: String) {

    UN_CMD(0xF0.toByte(), "未知命令"),

    TLS_FAIL_CMD(0x15.toByte(), "TLS异常"),
    HAND_SHAKE(0x1A.toByte(), "握手命令"),

    UNLOCK_CMD(0x01.toByte(), "开锁命令"),
    BLOCK_LIST_CMD(0x04.toByte(), "发送黑名单"),

    SCHEDULE_CMD(0x1B.toByte(), "设置时间段"),
    HOLIDAY_CMD(0x1C.toByte(), "设置节假日"),


    FIRE_REPORT_CMD(0x2B,"火警上报"),
    FIRE_RESET_CMD(0x2C,"火警复位"),


    REST_DEVICE_CMD(0x10.toByte(),"恢复出厂设置"),
    SET_TIME_CMD(0x21.toByte(), "设置时间"),
    DEVICE_DFU_CMD(0x05.toByte(), "DFU固件升級"),

    DEVICE_TIME_CMD(0x08.toByte(), "读取设备时间"),

    DEVICE_FIRMWARE_VERSION_CMD(0x09.toByte(), "读取固件版本号"),
    DEVICE_MAC_CMD(0x2A.toByte(), "读取蓝牙MAC"),
    DEVICE_STATE_CMD(0xF1.toByte(), "读取设备状态");

    companion object {

        fun setValue(code: Byte): BleCmdEnum {

            for (value in BleCmdEnum.values()) {
                if (value.code == code) {
                    return value
                }
            }
            return UN_CMD
        }
    }

    override fun toString(): String {
        return "code : ${code.toInt()} , desc : ${desc}"
    }
}




