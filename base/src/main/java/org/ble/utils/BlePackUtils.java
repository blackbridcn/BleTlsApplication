package org.ble.utils;

import android.bluetooth.BluetoothGatt;

import org.utils.ByteHexUtils;

import java.lang.reflect.Method;


/**
 * Author: yuzzha
 * Date: 2021-04-12 12:01
 * Description:
 * Remark:
 */
public class BlePackUtils {


    public boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {

        }
        return false;
    }


    /**
     * 给待发送的加密信息转义并分包处理；
     * Ble 中规定每个数据包发送最多为20个字节
     *
     * @param source
     * @return 转义分包后的加密信息
     * 备注：根据source长度分进行拆分，
     */
    public static byte[][] splitMsgPackage(byte[] source) {
        String hex = ByteHexUtils.INSTANCE.byteArrayToHexString(source);
        int length = (source.length / 20) + 1;
        byte[][] results = new byte[length][];
        if (source.length > 20) {
            for (int i = 0; i < length; i++) {
                byte[] temps = null;
                if (source.length - 20 * i > 20) {
                    temps = new byte[20];
                    for (int j = 20 * i, k = 0; j < 20 * (i + 1) && k < 20; j++, k++) {
                        temps[k] = source[j];
                    }
                } else {
                    temps = new byte[source.length - 20 * i];
                    for (int j = 20 * i, k = 0; j < source.length && k < 20; j++, k++) {
                        temps[k] = source[j];
                    }
                }
                results[i] = temps;
            }
        } else {
            results[0] = source;
        }
        return results;
    }
}
