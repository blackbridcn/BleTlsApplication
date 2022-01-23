package org.ble.scan;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;

import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: yuzzha
 * Date: 2021-09-03 17:03
 * Description:  Android  5.O  LOLLIPOP API 21 版本 以下(不包括API21) Bluetooth  Scan impl
 * Remark:
 */
class BluetoothScannerImplJB extends BluetoothScannerProvider {

    Map<BlzScanCallback, ScanCallbackWrapper> wrappers = new HashMap<>();

    private HandlerThread handlerThread;
    private Handler powerSaveHandler;

    private long powerSaveRestInterval;
    private long powerSaveScanInterval;

    /* package */ BluetoothScannerImplJB() {}

    @Override
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    protected void startScanInternal(List<ScanFilter> filters, ScanSettings settings, BlzScanCallback callback) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        boolean shouldStart = false;

        synchronized (wrappers) {
            if (!wrappers.containsKey(callback)) {
                //wrappers 建立 scanCallback wrapper 类
                ScanCallbackWrapper scanCallbackWrapper =
                        new ScanCallbackWrapper(false, false,
                        filters, settings, callback);
                shouldStart = wrappers.isEmpty();
                wrappers.put(callback, scanCallbackWrapper);
            }
        }

        if (handlerThread == null) {
            handlerThread = new HandlerThread(BluetoothScannerImplJB.class.getSimpleName());
            handlerThread.start();

            powerSaveHandler = new Handler(handlerThread.getLooper());
            // settings.
        }

        setPowerSaveSettings();

        if (shouldStart) {
            bluetoothAdapter.startLeScan(scanCallback);
        }
    }

    @Override
    protected void startScanInternal(List<ScanFilter> filters, ScanSettings settings, BlzScanCallback callback, Handler handler) {

    }

    @Override
    protected void stopScanInternal(BlzScanCallback callback) {

    }

    @Override
    void startScanInternal(@NonNull List<ScanFilter> filters, @NonNull ScanSettings settings, @NonNull Context context, @NonNull PendingIntent callbackIntent, int requestCode) {

    }

    @Override
    void stopScanInternal(@NonNull Context context, @NonNull PendingIntent callbackIntent, int requestCode) {

    }

    @Override
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public void flushPendingScanResults(@NonNull final BlzScanCallback callback) {
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null!");
        }
        ScanCallbackWrapper wrapper;
        synchronized (wrappers) {
            wrapper = wrappers.get(callback);
        }
        if (wrapper == null) {
            throw new IllegalArgumentException("callback not registered!");
        }
        wrapper.flushPendingScanResults();
    }


    private void setPowerSaveSettings() {
        long minRest = Long.MAX_VALUE;
        long minScan = Long.MAX_VALUE;
        synchronized (wrappers) {
            for (final ScanCallbackWrapper wrapper : wrappers.values()) {
                final ScanSettings settings = wrapper.scanSettings;
                if (settings.hasPowerSaveMode()) {
                    if (minRest > settings.getPowerSaveRest()) {
                        minRest = settings.getPowerSaveRest();
                    }
                    if (minScan > settings.getPowerSaveScan()) {
                        minScan = settings.getPowerSaveScan();
                    }
                }
            }
        }

        if (minRest < Long.MAX_VALUE && minScan < Long.MAX_VALUE) {
            powerSaveRestInterval = minRest;
            powerSaveScanInterval = minScan;
            if (powerSaveHandler != null) {
                powerSaveHandler.removeCallbacks(powerSaveScanTask);
                powerSaveHandler.removeCallbacks(powerSaveSleepTask);
                powerSaveHandler.postDelayed(powerSaveSleepTask, powerSaveScanInterval);
            }
        } else {
            powerSaveRestInterval = powerSaveScanInterval = 0;
            if (powerSaveHandler != null) {
                powerSaveHandler.removeCallbacks(powerSaveScanTask);
                powerSaveHandler.removeCallbacks(powerSaveSleepTask);
            }
        }
    }

    private final BluetoothAdapter.LeScanCallback scanCallback = (device, rssi, scanRecord) -> {
        final ScanResult scanResult = new ScanResult(device, ScanRecord.parseFromBytes(scanRecord),
                rssi, SystemClock.elapsedRealtimeNanos());

        synchronized (wrappers) {
            final Collection<ScanCallbackWrapper> scanCallbackWrappers = wrappers.values();
            for (final ScanCallbackWrapper wrapper : scanCallbackWrappers) {
                wrapper.handler.post(() -> wrapper.handleScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, scanResult));
            }
        }
    };

    //Android 5.0 版本以下 蓝牙扫描回调 实现类
    private final Runnable powerSaveScanTask = new Runnable() {
        @Override
        @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
        public void run() {
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && powerSaveRestInterval > 0 && powerSaveScanInterval > 0) {
                adapter.startLeScan(scanCallback);
                // 超时后，延时任务实现stop scan
                powerSaveHandler.postDelayed(powerSaveSleepTask, powerSaveScanInterval);
            }
        }
    };

    //Android 5.0 版本以下 停止蓝牙扫描Task
    private final Runnable powerSaveSleepTask = new Runnable() {
        @Override
        @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
        public void run() {
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && powerSaveRestInterval > 0 && powerSaveScanInterval > 0) {
                adapter.stopLeScan(scanCallback);
                powerSaveHandler.postDelayed(powerSaveScanTask, powerSaveRestInterval);
            }
        }
    };
    /*
    List<ScanFilter> filters, ScanSettings settings,
    final ScanCallback callback*/
}
