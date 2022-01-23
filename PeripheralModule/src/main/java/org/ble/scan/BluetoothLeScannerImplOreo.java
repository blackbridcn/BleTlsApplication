package org.ble.scan;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Author: yuzzha
 * Date: 2021-11-13 20:14
 * Description:
 *
 * <p>
 * <p>
 * Android  8.0 Oreo
 * <p>
 * google 在系统层更新了一个扫描API，系统层为你提供后台持续扫描的能力。
 * (即便APP已被杀死，扫描仍会继续。但如果用户重启或关闭蓝牙后，该扫描停止)
 *
 * </p>
 * <p>
 * Remark:
 */
@RequiresApi(api = Build.VERSION_CODES.O)
class BluetoothLeScannerImplOreo extends BluetoothLeScannerImplMarshmallow {

    /**
     * A map that stores {@link PendingIntentExecutorWrapper}s for user's {@link PendingIntent}.
     * Each wrapper keeps track of found and lost devices and allows to emulate batching.
     */
    // The type is HashMap, not Map, as the Map does not allow to put null as values.
    @NonNull
    private final HashMap<PendingIntent, PendingIntentExecutorWrapper> wrappers = new HashMap<>();

    @Nullable/* package */
    PendingIntentExecutorWrapper getWrapper(@NonNull final PendingIntent callbackIntent) {
        synchronized (wrappers) {
            if (wrappers.containsKey(callbackIntent)) {
                final PendingIntentExecutorWrapper wrapper = wrappers.get(callbackIntent);
                if (wrapper == null)
                    throw new IllegalStateException("Scanning has been stopped");
                return wrapper;
            }
            return null;
        }
    }

    /* package */
    void addWrapper(@NonNull final PendingIntent callbackIntent,
                    @NonNull final PendingIntentExecutorWrapper wrapper) {
        synchronized (wrappers) {
            wrappers.put(callbackIntent, wrapper);
        }
    }

    @Override /* package */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    void startScanInternal(@Nullable final List<ScanFilter> filters,
                           @Nullable final ScanSettings settings,
                           @NonNull final Context context,
                           @NonNull final PendingIntent callbackIntent,
                           final int requestCode) {

        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner == null)
            throw new IllegalStateException("BT le scanner not available");

        final ScanSettings nonNullSettings = settings != null ? settings : new ScanSettings.Builder().build();
        final List<ScanFilter> nonNullFilters = filters != null ? filters : Collections.emptyList();

        final android.bluetooth.le.ScanSettings nativeSettings = toNativeScanSettings(adapter, nonNullSettings, false);

        List<android.bluetooth.le.ScanFilter> nativeFilters = null;
        if (filters != null && adapter.isOffloadedFilteringSupported() && nonNullSettings.getUseHardwareFilteringIfSupported())
            nativeFilters = toNativeScanFilters(filters);

        synchronized (wrappers) {
            // Make sure there is not such callbackIntent in the map.
            // The value could have been set to null when the same intent was used before.
            wrappers.remove(callbackIntent);
        }

        final PendingIntent pendingIntent = createStartingPendingIntent(nonNullFilters,
                nonNullSettings, context, callbackIntent, requestCode);

        scanner.startScan(nativeFilters, nativeSettings, pendingIntent);
    }

    /* package */
      @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
      void stopScanInternal(@NonNull final Context context,
                                            @NonNull final PendingIntent callbackIntent,
                                            final int requestCode) {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner == null)
            throw new IllegalStateException("BT le scanner not available");

        final PendingIntent pendingIntent = createStoppingPendingIntent(context, requestCode);
        scanner.stopScan(pendingIntent);

        synchronized (wrappers) {
            // Do not remove the key, just set the value to null.
            // Based on that we will know that scanning has been stopped.
            // This is used to discard scanning results delivered after the scan was stopped.
            // Unfortunately, the callbackIntent will have to be kept and won't he removed,
            // despite the fact that reports will eventually stop being broadcast.
            wrappers.put(callbackIntent, null);
        }
    }


    @NonNull
    private PendingIntent createStartingPendingIntent(@NonNull final List<ScanFilter> filters,
                                                      @NonNull final ScanSettings settings,
                                                      @NonNull final Context context,
                                                      @NonNull final PendingIntent callbackIntent,
                                                      final int requestCode) {
        // Since Android 8 it has to be an explicit intent
        final Intent intent = new Intent(context, PendingIntentReceiver.class);
        intent.setAction(PendingIntentReceiver.ACTION);

        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        // The caller's callbackIntent will be used to send the intent to the app
        intent.putExtra(PendingIntentReceiver.EXTRA_PENDING_INTENT, callbackIntent);

        // The following extras will be used to filter and batch data if needed,
        // that is when ScanSettings.Builder#use[...]IfSupported were called with false.
        // Only native classes may be used here, as they are delivered to another application.

        intent.putParcelableArrayListExtra(PendingIntentReceiver.EXTRA_FILTERS, toNativeScanFilters(filters));

        intent.putExtra(PendingIntentReceiver.EXTRA_SETTINGS, toNativeScanSettings(adapter, settings, true));
        intent.putExtra(PendingIntentReceiver.EXTRA_USE_HARDWARE_BATCHING, settings.getUseHardwareBatchingIfSupported());
        intent.putExtra(PendingIntentReceiver.EXTRA_USE_HARDWARE_FILTERING, settings.getUseHardwareFilteringIfSupported());
        intent.putExtra(PendingIntentReceiver.EXTRA_USE_HARDWARE_CALLBACK_TYPES, settings.getUseHardwareCallbackTypesIfSupported());
        intent.putExtra(PendingIntentReceiver.EXTRA_MATCH_MODE, settings.getMatchMode());
        intent.putExtra(PendingIntentReceiver.EXTRA_NUM_OF_MATCHES, settings.getNumOfMatches());

        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * When scanning with PendingIntent on Android Oreo or newer, the app may get killed
     */
    @NonNull
    private PendingIntent createStoppingPendingIntent(@NonNull final Context context,
                                                      final int requestCode) {
        // Since Android 8 it has to be an explicit intent
        final Intent intent = new Intent(context, PendingIntentReceiver.class);
        intent.setAction(PendingIntentReceiver.ACTION);

        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    /* package */ android.bluetooth.le.ScanSettings toNativeScanSettings(@NonNull final BluetoothAdapter adapter,
                                                                         @NonNull final ScanSettings settings,
                                                                         final boolean exactCopy) {
        final android.bluetooth.le.ScanSettings.Builder builder =
                new android.bluetooth.le.ScanSettings.Builder();

        if (exactCopy || adapter.isOffloadedScanBatchingSupported() && settings.getUseHardwareBatchingIfSupported())
            builder.setReportDelay(settings.getReportDelayMillis());

        if (exactCopy || settings.getUseHardwareCallbackTypesIfSupported())
            builder.setCallbackType(settings.getCallbackType())
                    .setMatchMode(settings.getMatchMode())
                    .setNumOfMatches(settings.getNumOfMatches());

        builder.setScanMode(settings.getScanMode())
                .setLegacy(settings.getLegacy())
                .setPhy(settings.getPhy());

        return builder.build();
    }

    @NonNull
        /* package */ ScanSettings fromNativeScanSettings(@NonNull final android.bluetooth.le.ScanSettings settings,
                                                          final boolean useHardwareBatchingIfSupported,
                                                          final boolean useHardwareFilteringIfSupported,
                                                          final boolean useHardwareCallbackTypesIfSupported,
                                                          final long matchLostDeviceTimeout,
                                                          final long matchLostTaskInterval,
                                                          final int matchMode, final int numOfMatches) {
        final ScanSettings.Builder builder = new ScanSettings.Builder()
                .setLegacy(settings.getLegacy())
                .setPhy(settings.getPhy())
                .setCallbackType(settings.getCallbackType())
                .setScanMode(settings.getScanMode())
                .setReportDelay(settings.getReportDelayMillis())
                .setUseHardwareBatchingIfSupported(useHardwareBatchingIfSupported)
                .setUseHardwareFilteringIfSupported(useHardwareFilteringIfSupported)
                .setUseHardwareCallbackTypesIfSupported(useHardwareCallbackTypesIfSupported)
                .setMatchOptions(matchLostDeviceTimeout, matchLostTaskInterval)
                // Those 2 values are not accessible from the native ScanSettings.
                // They need to be transferred separately in intent extras.
                .setMatchMode(matchMode).setNumOfMatches(numOfMatches);

        return builder.build();
    }

    @NonNull
        /* package */ ArrayList<ScanFilter> fromNativeScanFilters(@NonNull final List<android.bluetooth.le.ScanFilter> filters) {
        final ArrayList<ScanFilter> nativeScanFilters = new ArrayList<>();
        for (final android.bluetooth.le.ScanFilter filter : filters)
            nativeScanFilters.add(fromNativeScanFilter(filter));
        return nativeScanFilters;
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
        /* package */ ScanFilter fromNativeScanFilter(@NonNull final android.bluetooth.le.ScanFilter filter) {
        final ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setDeviceAddress(filter.getDeviceAddress())
                .setDeviceName(filter.getDeviceName())
                .setServiceUuid(filter.getServiceUuid(), filter.getServiceUuidMask())
                .setManufacturerData(filter.getManufacturerId(), filter.getManufacturerData(), filter.getManufacturerDataMask());

        if (filter.getServiceDataUuid() != null)
            builder.setServiceData(filter.getServiceDataUuid(), filter.getServiceData(), filter.getServiceDataMask());

        return builder.build();
    }

    @NonNull
    @Override
        /* package */ ScanResult fromNativeScanResult(@NonNull final android.bluetooth.le.ScanResult result) {
        // Calculate the important bits of Event Type
        final int eventType = (result.getDataStatus() << 5)
                | (result.isLegacy() ? ScanResult.ET_LEGACY_MASK : 0)
                | (result.isConnectable() ? ScanResult.ET_CONNECTABLE_MASK : 0);
        // Get data as bytes
        final byte[] data = result.getScanRecord() != null ? result.getScanRecord().getBytes() : null;
        // And return the v18.ScanResult
        return new ScanResult(result.getDevice(), eventType, result.getPrimaryPhy(),
                result.getSecondaryPhy(), result.getAdvertisingSid(),
                result.getTxPower(), result.getRssi(),
                result.getPeriodicAdvertisingInterval(),
                ScanRecord.parseFromBytes(data), result.getTimestampNanos());
    }

    /* package */ static class PendingIntentExecutorWrapper extends ScanCallbackWrapper {
        /* package */
        @NonNull
        final PendingIntentExecutor executor;

        PendingIntentExecutorWrapper(final boolean offloadedBatchingSupported,
                                     final boolean offloadedFilteringSupported,
                                     @NonNull final List<ScanFilter> filters,
                                     @NonNull final ScanSettings settings,
                                     @NonNull final PendingIntent callbackIntent) {
            super(offloadedBatchingSupported, offloadedFilteringSupported, filters, settings,
                    new PendingIntentExecutor(callbackIntent, settings), new Handler());

            executor = (PendingIntentExecutor) scanCallback;
        }
    }
}
