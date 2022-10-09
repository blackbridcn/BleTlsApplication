package org.ble.config;

import org.ble.exception.BleException;
import org.ble.utils.ObjectHelp;

/**
 * Author: yuzzha
 * Date: 2021-04-09 11:51
 * Description:
 * Remark:
 */
public final class BleConfig {

    //蓝牙扫描超时默认为500ms
    long scanTimeout = 30 * 1000;
    String serviceUuid;

    String rxCharacterUuid;
    String txCharacterUuid;
    public static int BLE_RSSI_THRESHOLD_VALUE = -70;
    /**
     * valid Bluetooth names are a maximum of 248 bytes using UTF-8 encoding,
     * although many remote devices can only display the first 40 characters,
     * and some may be limited to just 20.
     */
    String bleName;

    boolean peripheral;

    public BleConfig(String bleName, long scanTimeout, String serviceUUID, String txCharacterUuid, String rxCharacterUuid, boolean peripheral) {
        this.bleName = bleName;
        this.scanTimeout = scanTimeout;
        this.serviceUuid = serviceUUID;
        this.txCharacterUuid = txCharacterUuid;
        this.rxCharacterUuid = rxCharacterUuid;
        this.peripheral = peripheral;
    }

    public BleConfig(Builder builder) {
        this.bleName = builder.bleName;
        this.scanTimeout = builder.scanTimeout;
        this.serviceUuid = builder.serviceUuid;
        this.txCharacterUuid = builder.txCharacterUuid;
        this.rxCharacterUuid = builder.rxCharacterUuid;
        this.peripheral = builder.peripheral;
    }

    public long getScanTimeout() {
        return scanTimeout;
    }

    public void setScanTimeout(long scanTimeout) {
        this.scanTimeout = scanTimeout;
    }

    public String getServiceUuid() {
        return serviceUuid;
    }

    public void setServiceUuid(String serviceUuid) {
        this.serviceUuid = serviceUuid;
    }

    public String getRxCharacterUuid() {
        return rxCharacterUuid;
    }

    public void setRxCharacterUuid(String rxCharacterUuid) {
        this.rxCharacterUuid = rxCharacterUuid;
    }

    public String getTxCharacterUuid() {
        return txCharacterUuid;
    }

    public void setTxCharacterUuid(String txCharacterUuid) {
        this.txCharacterUuid = txCharacterUuid;
    }

    public String getBleName() {
        return bleName;
    }

    public void setBleName(String bleName) {
        this.bleName = bleName;
    }

    public boolean isPeripheral() {
        return peripheral;
    }

    public void setPeripheral(boolean peripheral) {
        this.peripheral = peripheral;
    }

    public static final class Builder {
        long scanTimeout;
        String serviceUuid;

        String rxCharacterUuid;
        String txCharacterUuid;

        String bleName;
        boolean newApi = true;
        boolean peripheral;

        Builder(long scanTimeout) {
            this.scanTimeout = scanTimeout;
        }

        public Builder() {
            this(5 * 100);
        }


        public Builder setScanTimeout(long timeout) {
            this.scanTimeout = timeout;
            return this;
        }

        public Builder serviceUuid(String serviceUuid) {
            this.serviceUuid = serviceUuid;
            return this;
        }

        public Builder rxCharacterUuid(String rxCharacterUuid) {
            this.rxCharacterUuid = rxCharacterUuid;
            return this;
        }

        public Builder txCharacterUuid(String txCharacterUuid) {
            this.txCharacterUuid = txCharacterUuid;
            return this;
        }

        public Builder bleName(String bleName) {
            this.bleName = bleName;
            return this;
        }

        public Builder isNewApi(boolean newApi) {
            this.newApi = newApi;
            return this;
        }

        public Builder isPeripheral(boolean peripheral) {
            this.peripheral = peripheral;
            return this;
        }

        public BleConfig build() {
            if (ObjectHelp.checkNotNullEmpty(serviceUuid, rxCharacterUuid, txCharacterUuid))
                return new BleConfig(this);
            else throw new BleException(" must set serviceUUID and characterUUID value !");
        }
    }

}
