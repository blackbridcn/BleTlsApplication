package com.hidglobal.duality.testapplication.message;


import com.hidglobal.ui.ble.conn.enums.BleCmdEnum;
import com.hidglobal.ui.ble.conn.enums.BleEncryptType;

import org.utils.StringUtils;

/**
 * Author: yuzzha
 * Date: 2021-06-18 17:07
 * Description:
 * Remark:
 */
public class BlePackage {

    private static final String TAG = BlePackage.class.getSimpleName();
    private static final boolean debug = true;

    private String osi = Constants.PACKAGE_PRE_HEX;
    //1 byte
    private byte cmd;
    private BleCmdEnum cmdEnum;
    //2 byte
    private int dataLen;

    //n byte
    private String data;
    private byte[] body;

    private RelayRespCode errCode;

    //2 byte
    private boolean success;

    public BlePackage(BleCmdEnum cmd, byte[] data) {
        this.cmd = cmd.getCode();
        this.body = data;
    }

    public BlePackage(byte[] result, int len) {
        if (result[0] == Constants.PACKAGE_PRE) {
            int crc = CrcUtils.calcCrc(result, 1, len - 2);
            int srcCrc = mergeByte(result[len - 1], result[len - 2]);
            if (srcCrc == crc) {
                this.success = true;
            } else {
                this.success = false;
                errCode = RelayRespCode.ERR_CRC;
            }
            cmd = result[1];
            cmdEnum = BleCmdEnum.Companion.setValue(result[1]);
            this.dataLen = len - 6;
            body = new byte[dataLen];
            System.arraycopy(result, 4, body, 0, dataLen);
            this.data = HexUtils.bytesToHexString(body);

            if (dataLen >= 1) {
                if (body[0] == RelayRespCode.ERR_OK.getCode() || body[0] == RelayRespCode.ERR_OPENING.getCode()) {
                    success = true;
                } else if (result[1] == BleCmdEnum.HAND_SHAKE.getCode() ||

                        result[1] == BleCmdEnum.DEVICE_TIME_CMD.getCode() ||
                        result[1] == BleCmdEnum.DEVICE_MAC_CMD.getCode() ||
                        result[1] == BleCmdEnum.DEVICE_FIRMWARE_VERSION_CMD.getCode()

                ) {
                    success = true;
                } else if (result[1] == BleCmdEnum.BLOCK_LIST_CMD.getCode() && RelayRespCode.ERR_VERSION.getCode() == body[0]) {
                    success = true;
                } else {
                    errCode = RelayRespCode.Companion.setValue(body[0]);
                    success = false;
                }
            }
        }
    }

    private int mergeByte(byte h, byte l) {
        return (l & 0xFF) | ((h & 0xFF) << 8);
    }

    public byte[] buildPackage() {
        int len = calcBodyLen();
        int dataLen = len + 6;
        byte[] pack = new byte[dataLen];
        pack[0] = Constants.PACKAGE_PRE;
        pack[1] = getCmd();
        //length 低位在前 ，高位在后
        pack[2] = (byte) (len & 0xFF);
        pack[3] = (byte) ((len & 0xFF00) >> 8);
        if (len > 0) {
            System.arraycopy(getBody(), 0, pack, 4, len);
        }
       /* int expend = 0;
        for (int i = 1; i < len; i++) {
            if (pack[i] == Constants.PACKAGE_PRE) {
                expend++;
            }
        }
        expend = 0;
        if (expend > 0) {
            byte[] newPack = new byte[len + 3 + expend];
            newPack[0] = Constants.PACKAGE_PRE;
            int newPi = 1;
            int packP = 1;
            while (packP < len + 1) {
                if (pack[packP] == Constants.PACKAGE_PRE) {
                    newPack[newPi] = Constants.PACKAGE_SUB_0;
                    newPi++;
                    newPack[newPi] = Constants.PACKAGE_SUB_1;
                } else {
                    newPack[newPi] = pack[packP];
                }
                newPi++;
                packP++;
            }

            int crc = CrcUtils.calcCrc(newPack, 1, len + expend);
            newPack[len + 2 + expend] = (byte) ((crc >> 8) & 0xFF);
            newPack[len + 1 + expend] = (byte) (crc & 0xFF);

            System.out.println(" Source package Hex :" + HexUtils.bytesToHexString(pack) + "  expend :" + expend);
            System.out.println(" newPack  Hex :" + HexUtils.bytesToHexString(newPack));
            return newPack;
        }*/
        // System.out.println(" Source package Hex :" + HexUtils.bytesToHexString(pack) + "  expend :" + expend);
        int crc = CrcUtils.calcCrc(pack, 1, dataLen - 2);
        pack[dataLen - 1] = (byte) ((crc >> 8) & 0xFF);
        pack[dataLen - 2] = (byte) (crc & 0xFF);
        return pack;
    }

    private int calcBodyLen() {
        if (StringUtils.isEmpty(this.data)) {
            return (body == null ? 0 : body.length);
        }
        return (StringUtils.isEmpty(this.data) ? 0 : this.data.length() / 2) + 3;
    }


    public String getOsi() {
        return osi;
    }

    public void setOsi(String osi) {
        this.osi = osi;
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public boolean isSuccess() {
        return success;
    }

    public BleCmdEnum getCmdEnum() {
        return cmdEnum;
    }

    public void setCmdEnum(BleCmdEnum cmdEnum) {
        this.cmdEnum = cmdEnum;
    }

    public RelayRespCode getErrCode() {
        return errCode;
    }

    public void setErrCode(RelayRespCode errCode) {
        this.errCode = errCode;
    }

    public BleEncryptType encryptType() {
        if (this.cmd == BleCmdEnum.HAND_SHAKE.getCode()) {
            return BleEncryptType.Companion.setValue(body[0]);
        }
        return BleEncryptType.NULL_ENCRYPT;
    }

    @Override
    public String toString() {
        return "PackageData{" +
                ", success =" + isSuccess() +
                ", cmd=" + cmdEnum.toString() +
                ", len='" + dataLen + '\'' +
                ", data='" + data + '\'' +
                // ", crc='" + crc + '\'' +
                '}';
    }
}
