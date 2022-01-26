package com.hidglobal.duality.testapplication.message;


import com.hidglobal.ui.ble.conn.enums.BleCmdEnum;

import java.util.Calendar;
import java.util.Date;

/**
 * Author: yuzzha
 * Date: 2021-06-22 9:16
 * Description:
 * Remark:
 */
public class BleDataUtils {



    public static byte[] buildUnlockMsg(Date time){
        Calendar c = Calendar.getInstance();
        if (time == null) {
            time = new java.sql.Date(System.currentTimeMillis());
        }
        c.setTime(time);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int weekday = c.get(Calendar.DAY_OF_WEEK);
        if (weekday > 1) {
            weekday = weekday - 1;
        } else {
            weekday = 7;
        }
        StringBuilder dateStr = new StringBuilder(16);
        dateStr.append("01")
                .append(HexUtils.buildComplLen(String.valueOf(second), 2))
                .append(HexUtils.buildComplLen(String.valueOf(minute), 2))
                .append(HexUtils.buildComplLen(String.valueOf(hour), 2))
                .append(HexUtils.buildComplLen(String.valueOf(day), 2))
                .append(HexUtils.buildComplLen(String.valueOf(month + 1), 2))
                .append(String.valueOf(year).substring(2, 4))
                .append("0".concat(String.valueOf(weekday)));
        String hexDate = dateStr.toString();
        byte[] msg = HexUtils.HexString2Bytes(hexDate);
        BlePackage pack = new BlePackage(BleCmdEnum.UNLOCK_CMD, msg);
        return pack.buildPackage();
        //7E 01 05 00 00 E2E8F445E5B8
    }

    public static byte[] buildSettingDateMsg(Date time) {
        Calendar c = Calendar.getInstance();
        if (time == null) {
            time = new java.sql.Date(System.currentTimeMillis());
        }
        c.setTime(time);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int weekday = c.get(Calendar.DAY_OF_WEEK);
        if (weekday > 1) {
            weekday = weekday - 1;
        } else {
            weekday = 7;
        }
        StringBuilder dateStr = new StringBuilder(14);
        dateStr.append(HexUtils.buildComplLen(String.valueOf(second), 2))
                .append(HexUtils.buildComplLen(String.valueOf(minute), 2))
                .append(HexUtils.buildComplLen(String.valueOf(hour), 2))
                .append(HexUtils.buildComplLen(String.valueOf(day), 2))
                .append(HexUtils.buildComplLen(String.valueOf(month + 1), 2))
                .append(String.valueOf(year).substring(2, 4))
                .append("0".concat(String.valueOf(weekday)));
        String hexDate = dateStr.toString();
        byte[] msg = HexUtils.HexString2Bytes(hexDate);
        BlePackage pack = new BlePackage(BleCmdEnum.SET_TIME_CMD, msg);
        return pack.buildPackage();
    }






}
