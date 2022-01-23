package org.ble.utils;

/**
 * Author: yuzzha
 * Date: 2021-11-27 21:05
 * Description:
 * Remark:
 * RSSI（接收信号强度）Received Signal Strength Indicator
 * Rss=10logP,
 * 只需将接受到的信号功率P代入就是接收信号强度（灵敏度）。
 * [例1] 如果发射功率P为1mw，折算为dBm后为0dBm。
 * [例2] 对于40W的功率，按dBm单位进行折算后的值应为：
 * 10lg（40W/1mw)=10lg（40000）=10lg4+10lg10+10lg1000=46dBm。
 *
 * Rssi和接收功率有关，单位是dBm
 * 一般为负值，反应的是信号的衰减程度，理想状态下（无衰减），Rssi = 0dBm，实际情况是，即使蓝牙设备挨得非常近，Rssi也只有-50dBm的强度，在传输过程中，不可避免要损耗。
 * 一般情况下，经典蓝牙强度
 * -50 ~ 0dBm 信号强
 * -70 ~-50dBm 信号中
 * <-70dBm 信号弱
 *
 * 低功耗蓝牙分四级
 * -60 ~ 0 4
 * -70 ~ -60 3
 * -80 ~ -70 2
 * <-80 1
 */
/**
 * A和n的值，需要根据实际环境进行检测得出
 */
public class RssiUtils {
    /** A 发射端和接收端相隔1米时的信号强度 */
    private static final double A_Value = 50;
    /** n 环境衰减因子 */
    private static final double n_Value = 2.5;

    /**
     * 根据Rssi的值，计算距离，单位m
     * @param rssi 信号强度，单位dB
     */
    public static double getLeDistance(int rssi) {
        double power = (Math.abs(rssi) - A_Value) / (10 * n_Value);
        return Math.pow(10, power);
    }

    /**
     * 经典蓝牙强度
     * -50 ~ 0dBm  信号强
     * -70 ~ -50dBm    信号中
     * <-70dBm      信号弱
     */
    public static byte getBredrLevel(int rssi) {
        if(rssi > -50) {
            return 3;
        } else if(rssi > -70) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * 低功耗蓝牙分四级
     * -60 ~ 0     4
     * -70 ~ -60   3
     * -80 ~ -70   2
     * <-80         1
     */
    public static byte getLeLevel(int rssi) {
        if(rssi > -60) {
            return 4;
        } else if(rssi > -70) {
            return 3;
        } else if(rssi > -80) {
            return 2;
        } else {
            return 1;
        }
    }
}
