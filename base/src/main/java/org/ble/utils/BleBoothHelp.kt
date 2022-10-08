package org.ble.utils

import android.bluetooth.BluetoothGatt
import org.jetbrains.annotations.NotNull

/**
 * Author: yuzzha
 * Date: 2019/4/25 17:08
 * Description: ${DESCRIPTION}
 * Remark:
 */
class BleBoothHelp private constructor() {

    companion object {
        /**
         * 给待发送的加密信息转义并分包处理；
         * Ble 中规定每个数据包发送最多为20个字节
         *
         * @param source
         * @return 转义分包后的加密信息
         * 备注：根据source长度分进行拆分，
         */
        @JvmStatic
        fun splitMsgPackage(source: ByteArray,maxMtu :Int): Array<ByteArray?> {
            val length = source.size / maxMtu + 1
            val results = arrayOfNulls<ByteArray>(length)
            if (source.size > maxMtu) {
                for (i in 0 until length) {
                    var temps: ByteArray? = null
                    if (source.size - maxMtu * i > maxMtu) {
                        temps = ByteArray(maxMtu)
                        var j = maxMtu * i
                        var k = 0
                        while (j < maxMtu * (i + 1) && k < maxMtu) {
                            temps[k] = source[j]
                            j++
                            k++
                        }
                    } else {
                        temps = ByteArray(source.size - maxMtu * i)
                        var j = maxMtu * i
                        var k = 0
                        while (j < source.size && k < maxMtu) {
                            temps[k] = source[j]
                            j++
                            k++
                        }
                    }
                    results[i] = temps
                }
            } else {
                results[0] = source
            }
            return results
        }

        fun subPackageTask(@NotNull rawPackage: ByteArray): Array<ByteArray?> {
            val len = rawPackage.size
            val count = len / 20 + 1
            val results = arrayOfNulls<ByteArray>(len / 20 + 1)
            if (len > 20) {
                for (i in 0 .. count-1) {
                    var tem: ByteArray? = null
                    var size = 0
                    if (len - i * 20 > 20) {
                        size = 20
                        tem = ByteArray(size)
                    } else {
                        size = len - i * 20
                        tem = ByteArray(size)
                    }
                    for (j in 0 until size) {
                        tem[j] = rawPackage[i * 20 + j]
                    }
                    results[i] = tem
                }
            } else {
                results[0] = rawPackage
            }
            return results
        }


        fun refreshDeviceCache(gatt: BluetoothGatt): Boolean {
            try {
                val localMethod = gatt.javaClass.getMethod("refresh", *arrayOfNulls(0))
                if (localMethod != null) {
                    return (localMethod.invoke(gatt, *arrayOfNulls(0)) as Boolean)
                }
            } catch (localException: Exception) {
            }
            return false
        }
    }
}