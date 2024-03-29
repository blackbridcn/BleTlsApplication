package org.ble.utils

import java.lang.StringBuilder

/**
 * Author: yuzzha
 * Date: 2021-04-12 11:56
 * Description:
 * Remark:
 */
object HexStrUtils {

    fun hexToByteArray(src: String?): ByteArray? {
        if (src == null || src.length == 0) {
            return null
        }
        var data = src.toUpperCase()
        var size = src.length / 2
        var result = ByteArray(size)
        var cAny = data.toCharArray()
        for (i in 0..(size - 1)) {
            val poi = i * 2
            result[i] = (charToByte(cAny[poi]).toInt() shl 4).or(charToByte(cAny[poi + 1]).toInt())
                .and(0xFF).toByte()
        }
        return result
    }

    private fun charToByte(c: Char): Byte {
        return "0123456789ABCDEF".indexOf(c).toByte()
    }

    fun byteArrayToHexString(src: ByteArray?): String{
        var builder = StringBuilder()
        if (src == null || src.size <= 0) {
            return ""
        }
        var temp: Int
        for (i in src.indices) {
            temp = src[i].toInt()
            if (temp < 0) {
                temp = temp + 256
            }
            var index = temp and 0xFF
            val hex = Integer.toHexString(index)
            if (hex.length < 2) {
                builder.append("0")
            }
            builder.append(hex)
        }
        return builder.toString().uppercase()
    }

}