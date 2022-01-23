package org.ble.scan;

import android.Manifest;

import android.app.PendingIntent;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;


import org.ble.utils.ObjectHelp;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Author: yuzzha
 * Date: 2021-09-03 15:38
 * Description:
 * Remark: 蓝牙扫描适配  Provider
 *
 * Android 7.0 后不能在30秒内扫描+停止超过5次 ,
 * Android 10 进行BLE扫描时需要打开GPS。
 *
 * 参照 ：https://github.com/NordicSemiconductor/Android-DFU-Library
 */
public abstract class BluetoothScannerProvider {

    public static final String EXTRA_LIST_SCAN_RESULT = "android.bluetooth.le.extra.LIST_SCAN_RESULT";

    public static final String EXTRA_ERROR_CODE = "android.bluetooth.le.extra.ERROR_CODE";

    public static final String EXTRA_CALLBACK_TYPE = "android.bluetooth.le.extra.CALLBACK_TYPE";


    protected static BluetoothScannerProvider scanner;

    public synchronized static BluetoothScannerProvider getScanner() {
        if (scanner != null) {
            return scanner;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //26
            return scanner = new BluetoothLeScannerImplMarshmallow();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //23
            return scanner = new BluetoothLeScannerImplMarshmallow();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //21
            return scanner = new BluetoothLeScannerImplLollipop();
        } else {
            scanner = new BluetoothScannerImplJB();
        }
        return scanner;
    }


    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    public final void startScan(BlzScanCallback callback) {
        ObjectHelp.checkNotNull(callback, "callback can‘t  null");
        startScanInternal(Collections.emptyList(), new ScanSettings.Builder().build(),
                callback);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    public final void startScan(List<ScanFilter> filters, ScanSettings settings, BlzScanCallback callback) {
        ObjectHelp.checkNotNull(callback, "callback can‘t  null");
        startScanInternal(filters, settings,
                callback);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN})
    public final void stopScan(BlzScanCallback callback) {
        stopScanInternal(callback);
    }


    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    protected abstract void startScanInternal(List<ScanFilter> filters, ScanSettings settings,
                                              final BlzScanCallback callback);

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    protected abstract void startScanInternal(List<ScanFilter> filters, ScanSettings settings,
                                              final BlzScanCallback callback, Handler handler);

    protected abstract void stopScanInternal(BlzScanCallback callback);

    /*------------------------------------ Android  8.0 Oreo 新增 Scan API -------------------------------------------------------------*/

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    public final void startScan(@Nullable final List<ScanFilter> filters,
                                @Nullable final ScanSettings settings,
                                @NonNull final Context context,
                                @NonNull final PendingIntent callbackIntent,
                                final int requestCode) {
        //noinspection ConstantConditions
        if (callbackIntent == null) {
            throw new IllegalArgumentException("callbackIntent is null");
        }
        //noinspection ConstantConditions
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }
        startScanInternal(filters != null ? filters : Collections.emptyList(),
                (settings != null) ? settings : new ScanSettings.Builder().build(),
                context, callbackIntent, requestCode);
    }

    /* package */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    abstract void startScanInternal(@NonNull List<ScanFilter> filters,
                                    @NonNull ScanSettings settings,
                                    @NonNull Context context,
                                    @NonNull PendingIntent callbackIntent,
                                    final int requestCode);


    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    public final void stopScan(@NonNull final Context context,
                               @NonNull final PendingIntent callbackIntent,
                               final int requestCode) {
        //noinspection ConstantConditions
        if (callbackIntent == null) {
            throw new IllegalArgumentException("callbackIntent is null");
        }
        //noinspection ConstantConditions
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }
        stopScanInternal(context, callbackIntent, requestCode);
    }

    /* package */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    abstract void stopScanInternal(@NonNull Context context,
                                   @NonNull PendingIntent callbackIntent,
                                   final int requestCode);

    public abstract void flushPendingScanResults(@NonNull BlzScanCallback callback);


    static class ScanCallbackWrapper {

        @NonNull
        private final Object LOCK = new Object();

        private final boolean emulateFiltering;

        private final boolean emulateFoundOrLostCallbackType;

        private final boolean emulateBatching;

        /**
         * A collection of scan result of devices in range.
         */
        @NonNull
        private final Map<String, ScanResult> devicesInRange = new HashMap<>();

        @NonNull
        final List<ScanFilter> filters;

        @NonNull
        final ScanSettings scanSettings;

        @NonNull
        final BlzScanCallback scanCallback;

        private boolean scanningStopped;

        @NonNull
        final Handler handler;

        //scan 到的蓝牙设备 List
        @NonNull
        private final List<ScanResult> scanResults = new ArrayList<>();
        // Batch   n.	一批; (食物、药物等)一批生产的量; 批;
        @NonNull
        private final Set<String> devicesInBatch = new HashSet<>();

        ScanCallbackWrapper(final boolean offloadedBatchingSupported,
                            final boolean offloadedFilteringSupported,
                            @NonNull final List<ScanFilter> filters,
                            @NonNull final ScanSettings settings,
                            @NonNull final BlzScanCallback callback,
                            @NonNull final Handler handler) {

            this.filters = Collections.unmodifiableList(filters);
            this.scanSettings = settings;
            this.scanCallback = callback;
            this.scanningStopped = false;
            this.handler = handler;

            // Emulate other callback types
            final boolean callbackTypesSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
            emulateFoundOrLostCallbackType = settings.getCallbackType() != ScanSettings.CALLBACK_TYPE_ALL_MATCHES
                    && (!callbackTypesSupported || !settings.getUseHardwareCallbackTypesIfSupported());

            // Emulate filtering
            emulateFiltering = !filters.isEmpty() && (!offloadedFilteringSupported || !settings.getUseHardwareFilteringIfSupported());


            // Emulate batching
            final long delay = settings.getReportDelayMillis();
            emulateBatching = delay > 0 && (!offloadedBatchingSupported || !settings.getUseHardwareBatchingIfSupported());

            if (emulateBatching) {
                final Runnable flushPendingScanResultsTask = new Runnable() {
                    @Override
                    public void run() {
                        if (!scanningStopped) {
                            flushPendingScanResults();
                            handler.postDelayed(this, scanSettings.getReportDelayMillis());
                        }
                    }
                };
                handler.postDelayed(flushPendingScanResultsTask, delay);
            }


        }

        ScanCallbackWrapper(final boolean offloadedBatchingSupported,
                            final boolean offloadedFilteringSupported,
                            @NonNull final List<ScanFilter> filters,
                            @NonNull final ScanSettings settings,
                            @NonNull final BlzScanCallback callback) {
            this(offloadedBatchingSupported, offloadedFilteringSupported, filters, settings, callback, new Handler(Looper.getMainLooper()));
        }

        /* package */ void close() {
            scanningStopped = true;
            handler.removeCallbacksAndMessages(null);
            synchronized (LOCK) {
                devicesInRange.clear();
                devicesInBatch.clear();
                scanResults.clear();
            }
        }

        /* package */ void flushPendingScanResults() {
            if (emulateBatching && !scanningStopped) {
                synchronized (LOCK) {
                    scanCallback.onBatchScanResults(new ArrayList<>(scanResults));
                    scanResults.clear();
                    devicesInBatch.clear();
                }
            }
        }

        //将 scan 到的BluetoothDevice结果 回调给 观察者
        void handleScanResult(final int callbackType,
                              @NonNull final ScanResult scanResult) {
            if (scanningStopped || !filters.isEmpty() && !matches(scanResult))
                return;

            final String deviceAddress = scanResult.getDevice().getAddress();

            // Notify if a new device was found and callback type is FIRST MATCH
            if (emulateFoundOrLostCallbackType) { // -> Callback type != ScanSettings.CALLBACK_TYPE_ALL_MATCHES
                ScanResult previousResult;
                boolean firstResult;
                synchronized (devicesInRange) {
                    // The periodic task will be started only on the first result
                    firstResult = devicesInRange.isEmpty();
                    // Save the first result or update the old one with new data
                    previousResult = devicesInRange.put(deviceAddress, scanResult);
                }

                if (previousResult == null) {
                    if ((scanSettings.getCallbackType() & ScanSettings.CALLBACK_TYPE_FIRST_MATCH) > 0) {
                        scanCallback.onScanResult(ScanSettings.CALLBACK_TYPE_FIRST_MATCH, scanResult);
                    }
                }

                // In case user wants to be notified about match lost, we need to start a task that
                // will check the timestamp periodically
                if (firstResult) {
                    if ((scanSettings.getCallbackType() & ScanSettings.CALLBACK_TYPE_MATCH_LOST) > 0) {
                        handler.removeCallbacks(matchLostNotifierTask);
                        handler.postDelayed(matchLostNotifierTask, scanSettings.getMatchLostTaskInterval());
                    }
                }
            } else {
                // A callback type may not contain CALLBACK_TYPE_ALL_MATCHES and any other value.
                // If devicesInRange is empty, report delay > 0 means we are emulating hardware
                // batching. Otherwise handleScanResults(List) is called, not this method.
                if (emulateBatching) {
                    synchronized (LOCK) {
                        if (!devicesInBatch.contains(deviceAddress)) {  // add only the first record from the device, others will be skipped
                            scanResults.add(scanResult);
                            devicesInBatch.add(deviceAddress);
                        }
                    }
                    return;
                }

                scanCallback.onScanResult(callbackType, scanResult);
            }
        }

        //将 scan 到的所有BluetoothDevice结果 回调给 观察者
        /* package */ void handleScanResults(@NonNull final List<ScanResult> results) {
            if (scanningStopped)
                return;

            List<ScanResult> filteredResults = results;

            if (emulateFiltering) {
                filteredResults = new ArrayList<>();
                for (final ScanResult result : results)
                    if (matches(result))
                        filteredResults.add(result);
            }

            scanCallback.onBatchScanResults(filteredResults);
        }

        private boolean matches(@NonNull final ScanResult result) {
            for (final ScanFilter filter : filters) {
                if (filter.matches(result))
                    return true;
            }
            return false;
        }

        /* package */ void handleScanError(final int errorCode) {
            scanCallback.onScanFailed(errorCode);
        }


        /**
         * A task, called periodically, that notifies about match lost.
         */
        @NonNull
        private final Runnable matchLostNotifierTask = new Runnable() {
            @Override
            public void run() {
                final long now = SystemClock.elapsedRealtimeNanos();

                synchronized (LOCK) {
                    final Iterator<ScanResult> iterator = devicesInRange.values().iterator();
                    while (iterator.hasNext()) {
                        final ScanResult result = iterator.next();
                        // getMatchLostDeviceTimeout scan timeOut
                        if (result.getTimestampNanos() < now - scanSettings.getMatchLostDeviceTimeout()) {
                            iterator.remove();
                            handler.post(() -> scanCallback.onScanResult(ScanSettings.CALLBACK_TYPE_MATCH_LOST, result));
                        }
                    }

                    if (!devicesInRange.isEmpty()) {
                        handler.postDelayed(this, scanSettings.getMatchLostTaskInterval());
                    }
                }
            }
        };

    }
}
