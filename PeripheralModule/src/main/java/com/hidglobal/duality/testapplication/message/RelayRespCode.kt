package com.hidglobal.duality.testapplication.message

import androidx.annotation.StringRes
import com.hidglobal.duality.testapplication.R


/**
 * Author: yuzzha
 * Date: 2021-08-11 18:08
 * Description:
 * Remark:
 */
 public enum class RelayRespCode(val code: Int, @StringRes val desc: Int) {

    ERR_RESP_CODE(-1, 0/*R.string.module_relay_cmd_err_unknown_text*/),

    ERR_OK(1,6/* R.string.module_relay_cmd_err_ok_text*/),

    ERR_CRC(4, 0),
    ERR_OPENING(5, 1),
    ERR_VERSION(0x0E, 2);


    companion object {
        fun setValue(errCode: Int): RelayRespCode {
            for (codeEnum in values()) {
                if (codeEnum.code == errCode) {
                    return codeEnum
                }
            }
            return ERR_RESP_CODE
        }
    }
}