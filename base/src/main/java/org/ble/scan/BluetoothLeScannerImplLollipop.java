package org.ble.scan;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: yuzzha
 * Date: 2021-11-13 18:35
 * Description:
 * Remark:
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class BluetoothLeScannerImplLollipop extends BluetoothScannerProvider {

    @NonNull
    private final Map<BlzScanCallback, ScanCallbackWrapperLollipop> wrappers = new HashMap<>();

    /* package */ BluetoothLeScannerImplLollipop() {
    }

    @Override
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    protected void startScanInternal(List<ScanFilter> filters, ScanSettings settings, BlzScanCallback callback) {
        startScanInternal(filters, settings, callback, new Handler(Looper.getMainLooper()));
    }

    @Override
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    protected void startScanInternal(List<ScanFilter> filters, ScanSettings settings, BlzScanCallback callback, Handler handler) {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner == null)
            throw new IllegalStateException("BT le scanner not available");

        final boolean offloadedBatchingSupported = adapter.isOffloadedScanBatchingSupported();
        final boolean offloadedFilteringSupported = adapter.isOffloadedFilteringSupported();

        ScanCallbackWrapperLollipop wrapper;

        synchronized (wrappers) {
            if (wrappers.containsKey(callback)) {
                throw new IllegalArgumentException("scanner already started with given callback");
            }
            wrapper = new ScanCallbackWrapperLollipop(offloadedBatchingSupported,
                    offloadedFilteringSupported, filters, settings, callback, handler);
            wrappers.put(callback, wrapper);
        }

        final android.bluetooth.le.ScanSettings nativeScanSettings = toNativeScanSettings(adapter, settings, false);


        List<android.bluetooth.le.ScanFilter> nativeScanFilters = null;
        if (!filters.isEmpty() && offloadedFilteringSupported && settings.getUseHardwareFilteringIfSupported())
            nativeScanFilters = toNativeScanFilters(filters);

        scanner.startScan(nativeScanFilters, nativeScanSettings, wrapper.nativeCallback);
    }

    @Override
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    protected void stopScanInternal(BlzScanCallback callback) {
        ScanCallbackWrapperLollipop wrapper;
        synchronized (wrappers) {
            wrapper = wrappers.remove(callback);
        }
        if (wrapper == null)
            return;

        wrapper.close();

        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
            if (scanner != null){
                scanner.stopScan(wrapper.nativeCallback);
            }

        }
    }

    @Override
    void startScanInternal(@NonNull List<ScanFilter> filters, @NonNull ScanSettings settings, @NonNull Context context, @NonNull PendingIntent callbackIntent, int requestCode) {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();

        if (scanner == null)
            throw new IllegalStateException("BT le scanner not available");

        final Intent service = new Intent(context, ScannerService.class);
        service.putParcelableArrayListExtra(ScannerService.EXTRA_FILTERS, new ArrayList(filters));
        service.putExtra(ScannerService.EXTRA_SETTINGS, settings);
        service.putExtra(ScannerService.EXTRA_PENDING_INTENT, callbackIntent);
        service.putExtra(ScannerService.EXTRA_REQUEST_CODE, requestCode);
        service.putExtra(ScannerService.EXTRA_START, true);
        context.startService(service);
    }

    @Override
    void stopScanInternal(@NonNull Context context, @NonNull PendingIntent callbackIntent, int requestCode) {
        final Intent service = new Intent(context, ScannerService.class);
        service.putExtra(ScannerService.EXTRA_PENDING_INTENT, callbackIntent);
        service.putExtra(ScannerService.EXTRA_REQUEST_CODE, requestCode);
        service.putExtra(ScannerService.EXTRA_START, false);
        context.startService(service);
    }

    @Override
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    public void flushPendingScanResults(@NonNull BlzScanCallback callback) {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null!");
        }

        ScanCallbackWrapperLollipop wrapper;
        synchronized (wrappers) {
            wrapper = wrappers.get(callback);
        }

        if (wrapper == null) {
            throw new IllegalArgumentException("callback not registered!");
        }

        final ScanSettings settings = wrapper.scanSettings;
        if (adapter.isOffloadedScanBatchingSupported() && settings.getUseHardwareBatchingIfSupported()) {
            final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
            if (scanner == null)
                return;
            scanner.flushPendingScanResults(wrapper.nativeCallback);
        } else {
            wrapper.flushPendingScanResults();
        }
    }



    @NonNull
        /* package */ android.bluetooth.le.ScanSettings toNativeScanSettings(@NonNull final BluetoothAdapter adapter,
                                                                             @NonNull final ScanSettings settings,
                                                                             final boolean exactCopy) {
        final android.bluetooth.le.ScanSettings.Builder builder =
                new android.bluetooth.le.ScanSettings.Builder();
        if (exactCopy || adapter.isOffloadedScanBatchingSupported() && settings.getUseHardwareBatchingIfSupported())
            builder.setReportDelay(settings.getReportDelayMillis());

        if (settings.getScanMode() != ScanSettings.SCAN_MODE_OPPORTUNISTIC) {
            builder.setScanMode(settings.getScanMode());
        } else {
            // SCAN MORE OPPORTUNISTIC is not supported on Lollipop.
            // Instead, SCAN_MODE_LOW_POWER will be used.
            builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        }

        settings.disableUseHardwareCallbackTypes(); // callback types other then CALLBACK_TYPE_ALL_MATCHES are not supported on Lollipop

        return builder.build();
    }

    @NonNull
        /* package */ ArrayList<android.bluetooth.le.ScanFilter> toNativeScanFilters(@NonNull final List<ScanFilter> filters) {
        final ArrayList<android.bluetooth.le.ScanFilter> nativeScanFilters = new ArrayList<>();
        for (final ScanFilter filter : filters)
            nativeScanFilters.add(toNativeScanFilter(filter));
        return nativeScanFilters;
    }

    @NonNull
        /* package */ android.bluetooth.le.ScanFilter toNativeScanFilter(@NonNull final ScanFilter filter) {
        final android.bluetooth.le.ScanFilter.Builder builder = new android.bluetooth.le.ScanFilter.Builder();
        builder.setServiceUuid(filter.getServiceUuid(), filter.getServiceUuidMask())
                .setManufacturerData(filter.getManufacturerId(), filter.getManufacturerData(), filter.getManufacturerDataMask());

        if (filter.getDeviceAddress() != null)
            builder.setDeviceAddress(filter.getDeviceAddress());

        if (filter.getDeviceName() != null)
            builder.setDeviceName(filter.getDeviceName());

        if (filter.getServiceDataUuid() != null)
            builder.setServiceData(filter.getServiceDataUuid(), filter.getServiceData(), filter.getServiceDataMask());

        return builder.build();
    }


    // 将系统 android.bluetooth.le.ScanResult 转为自定义的 【重写】 org.ble.scanScanResult 对象
    @NonNull
    /* package */ ScanResult fromNativeScanResult(@NonNull final android.bluetooth.le.ScanResult nativeScanResult) {

        final byte[] data = nativeScanResult.getScanRecord() != null ?
                nativeScanResult.getScanRecord().getBytes() : null;

        return new ScanResult(nativeScanResult.getDevice(), ScanRecord.parseFromBytes(data),
                nativeScanResult.getRssi(), nativeScanResult.getTimestampNanos());
    }


    @NonNull
        /* package */ ArrayList<ScanResult> fromNativeScanResults(@NonNull final List<android.bluetooth.le.ScanResult> nativeScanResults) {
        final ArrayList<ScanResult> results = new ArrayList<>();
        for (final android.bluetooth.le.ScanResult nativeScanResult : nativeScanResults) {
            final ScanResult result = fromNativeScanResult(nativeScanResult);
            results.add(result);
        }
        return results;
    }

    static class ScanCallbackWrapperLollipop extends ScanCallbackWrapper {

        private ScanCallbackWrapperLollipop(final boolean offloadedBatchingSupported,
                                            final boolean offloadedFilteringSupported,
                                            @NonNull final List<ScanFilter> filters,
                                            @NonNull final ScanSettings settings,
                                            @NonNull final BlzScanCallback callback) {
            super(offloadedBatchingSupported, offloadedFilteringSupported,
                    filters, settings, callback);
        }


        private ScanCallbackWrapperLollipop(final boolean offloadedBatchingSupported,
                                            final boolean offloadedFilteringSupported,
                                            @NonNull final List<ScanFilter> filters,
                                            @NonNull final ScanSettings settings,
                                            @NonNull final BlzScanCallback callback,
                                            @NonNull final Handler handler) {
            super(offloadedBatchingSupported, offloadedFilteringSupported,
                    filters, settings, callback, handler);
        }

        @NonNull
        private final ScanCallback nativeCallback = new ScanCallback() {
            private long lastBatchTimestamp;

            @Override
            public void onScanResult(final int callbackType, final android.bluetooth.le.ScanResult nativeScanResult) {
                handler.post(() -> {
                    final BluetoothLeScannerImplLollipop scannerImpl =
                            (BluetoothLeScannerImplLollipop) BluetoothScannerProvider.getScanner();
                    final ScanResult result = scannerImpl.fromNativeScanResult(nativeScanResult);
                    handleScanResult(callbackType, result);
                });
            }

            @Override
            public void onBatchScanResults(final List<android.bluetooth.le.ScanResult> nativeScanResults) {
                handler.post(() -> {
                    // On several phones the onBatchScanResults is called twice for every batch.
                    // Skip the second call if came to early.
                    final long now = SystemClock.elapsedRealtime();
                    if (lastBatchTimestamp > now - scanSettings.getReportDelayMillis() + 5) {
                        return;
                    }
                    lastBatchTimestamp = now;

                    final BluetoothLeScannerImplLollipop scannerImpl =
                            (BluetoothLeScannerImplLollipop) BluetoothScannerProvider.getScanner();
                    final List<ScanResult> results = scannerImpl.fromNativeScanResults(nativeScanResults);
                    handleScanResults(results);
                });
            }

            @Override
            public void onScanFailed(final int errorCode) {
                handler.post(() -> {
                    // We were able to determine offloaded batching and filtering before we started scan,
                    // but there is no method checking if callback types FIRST_MATCH and MATCH_LOST
                    // are supported. We get an error here it they are not.
                    if (scanSettings.getUseHardwareCallbackTypesIfSupported()
                            && scanSettings.getCallbackType() != ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
                        // On Nexus 6 with Android 6.0 (MPA44G, M Pre-release 3) the errorCode = 5 (SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES)
                        // On Pixel 2 with Android 9.0 the errorCode = 4 (SCAN_FAILED_FEATURE_UNSUPPORTED)

                        // This feature seems to be not supported on your phone.
                        // Let's try to do pretty much the same in the code.
                        scanSettings.disableUseHardwareCallbackTypes();

                        //这里扫描失败后，再次重试机制
                        final BluetoothScannerProvider scanner = BluetoothScannerProvider.getScanner();
                        try {
                            //先Stop 正在scan的动作
                            scanner.stopScan(scanCallback);
                        } catch (final Exception e) {
                            // Ignore
                        }
                        try {
                            //再次 startScan
                            scanner.startScanInternal(filters, scanSettings, scanCallback, handler);
                        } catch (final Exception e) {
                            // Ignore
                        }
                        return;
                    }

                    // else, notify user application
                    handleScanError(errorCode);
                });
            }
        };


    }
}
