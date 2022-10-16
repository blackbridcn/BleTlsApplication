package org.ble;

import androidx.lifecycle.LiveData;

/**
 * Author: yuzzha
 * Date: 2022/4/11 11:06
 * Description:
 * Remark:
 */
public class ScannerStateLiveData extends LiveData<ScannerStateLiveData> {

    private boolean scanningStarted;
    private boolean hasRecords;
    private boolean bluetoothEnabled;
    private boolean locationEnabled;

    /* package */
    public ScannerStateLiveData(final boolean bluetoothEnabled,
                                final boolean locationEnabled) {
        this.scanningStarted = false;
        this.bluetoothEnabled = bluetoothEnabled;
        this.locationEnabled = locationEnabled;
        postValue(this);
    }

    public boolean isBluetoothEnabled() {
        return bluetoothEnabled;
    }

    public void bluetoothEnabled() {
        bluetoothEnabled = true;
        postValue(this);
    }

    /* package */
    public synchronized void bluetoothDisabled() {
        bluetoothEnabled = false;
        hasRecords = false;
        postValue(this);
    }

    /* package */
    public void setLocationEnabled(final boolean enabled) {
        locationEnabled = enabled;
        postValue(this);
    }


    public boolean locationEnabled(){
        return locationEnabled;
    }

    public  void scanningStarted() {
        scanningStarted = true;
        postValue(this);
    }

    public void scanningStopped() {
        scanningStarted = false;
        postValue(this);
    }
    /**
     * Returns whether scanning is in progress.
     */
    public  boolean isScanning() {
        return scanningStarted;
    }

}
