package org.ble.exception;

import com.hidglobal.duality.testapplication.R;

import org.ble.exception.BleErrorCode;

/**
 * Author: yuzzha
 * Date: 2019/4/25 15:37
 * Description: ${DESCRIPTION}
 * Remark:
 */
public enum EnumErrorCode {

    CONN_NO_FOUND_DEVICE_SERVICE(BleErrorCode.CONN_NO_FOUND_DEVICE_SERVICE, R.string.ble_no_find_service),
    CONN_NO_FOUND_DEVICE_UUID_SERVICE(BleErrorCode.CONN_NO_FOUND_DEVICE_UUID_SERVICE_ERROR, R.string.ble_no_find_uuid_service),
    CONN_SERVICE_STATE_ERROR(BleErrorCode.CONN_SERVICE_STATE_ERROR,R.string.ble_service_state_erro),
    CONN_WRITE_SERVICE_STATE_ERROR(BleErrorCode.CONN_WRITE_SERVICE_STATE_ERROR,R.string.ble_write_state_erro),
    CONN_READ_MSG_ERROR(BleErrorCode.CONN_READ_MSG_ERROR, R.string.ble_read_remote_msg_erro),
    CONN_DEVICE_CHARACTERISTIC_ERROR(BleErrorCode.CONN_DEVICE_CHARACTERISTIC_ERROR, R.string.ble_characteristic_erro),
    PARAM_ERROR(BleErrorCode.CODE_PARAM_ERROR, R.string.ble_param_error);


    int code;
    int descRes;

    EnumErrorCode(int code, int descRes) {
        this.code = code;
        this.descRes = descRes;
    }

    public static EnumErrorCode setValue(int code) {
        for (EnumErrorCode e : EnumErrorCode.values()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        return PARAM_ERROR;

    }

    public static int getDesc(int code) {
        for (EnumErrorCode e : EnumErrorCode.values()) {
            if (e.getCode() == code) {
                return e.getDesc();
            }
        }
        return PARAM_ERROR.getDesc();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getDesc() {
        return descRes;
    }

    public void setDesc(int desc) {
        this.descRes = desc;
    }
}
