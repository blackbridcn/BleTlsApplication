package com.hidglobal.duality.testapplication.message

/**
 * Author: yuzzha
 * Date: 2021/12/29 17:42
 * Description:
 * Remark:
 */
enum class BleEncryptType(var code: Byte, var desc: String) {

    NULL_ENCRYPT(8, "BLE明文"),
    TLS_V12_AES_ENCRYPT(1, "TLS1.2-对称AES"),
    AES_ENCRYPT(2, "BLE通信AES固定密钥对称加密"),
    TLS_V13_ENCRYPT(3, "TLS1.3");

    companion object {

        fun setValue(code: Byte): BleEncryptType {

            for (value in values()) {
                if (value.code == code) {
                    return value
                }
            }
            return NULL_ENCRYPT
        }
    }

    override fun toString(): String {
        return "code : ${code.toInt()} , desc : ${desc}"
    }
}