package org.ble.scan;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: yuzzha
 * Date: 2021-11-13 20:31
 * Description:
 * Remark:
 * <p>
 * A ScanCallback that will send a {@link PendingIntent} when callback's methods are called.
 */
/* package */class PendingIntentExecutor extends BlzScanCallback {

    @NonNull
    private final PendingIntent callbackIntent;

    /**
     * A temporary context given to the {@link android.content.BroadcastReceiver}.
     */
    @Nullable
    private Context context;
    /**
     * The service using this executor.
     */
    @Nullable
    private Context service;

    private long lastBatchTimestamp;
    private final long reportDelay;

    PendingIntentExecutor(@NonNull final PendingIntent callbackIntent,
                          @NonNull final ScanSettings settings) {
        this.callbackIntent = callbackIntent;
        this.reportDelay = settings.getReportDelayMillis();
    }


    PendingIntentExecutor(@NonNull final PendingIntent callbackIntent,
                          @NonNull final ScanSettings settings,
                          @NonNull final Service service) {
        this.callbackIntent = callbackIntent;
        this.reportDelay = settings.getReportDelayMillis();
        this.service = service;
    }

    /* package */ void setTemporaryContext(@Nullable final Context context) {
        this.context = context;
    }

    @Override
    public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
        final Context context = this.context != null ? this.context : this.service;
        if (context == null)
            return;

        try {
            final Intent extrasIntent = new Intent();
            extrasIntent.putExtra(BluetoothScannerProvider.EXTRA_CALLBACK_TYPE, callbackType);
            extrasIntent.putParcelableArrayListExtra(BluetoothScannerProvider.EXTRA_LIST_SCAN_RESULT,
                    new ArrayList<ScanResult>(Collections.singletonList(result)));

            callbackIntent.send(context, 0, extrasIntent);

        } catch (final PendingIntent.CanceledException e) {
            // Ignore
        }
    }

    @Override
    public void onBatchScanResults(@NonNull final List<ScanResult> results) {
        final Context context = this.context != null ? this.context : this.service;
        if (context == null)
            return;

        // On several phones the broadcast is sent twice for every batch.
        // Skip the second call if came to early.
        final long now = SystemClock.elapsedRealtime();
        if (lastBatchTimestamp > now - reportDelay + 5) {
            return;
        }
        lastBatchTimestamp = now;

        try {
            final Intent extrasIntent = new Intent();
            extrasIntent.putExtra(BluetoothScannerProvider.EXTRA_CALLBACK_TYPE,
                    ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
            extrasIntent.putParcelableArrayListExtra(BluetoothScannerProvider.EXTRA_LIST_SCAN_RESULT,
                    new ArrayList<Parcelable>(results));
            extrasIntent.setExtrasClassLoader(ScanResult.class.getClassLoader());
            callbackIntent.send(context, 0, extrasIntent);
        } catch (final PendingIntent.CanceledException e) {
            // Ignore
        }
    }

    @Override
    public void onScanFailed(final int errorCode) {
        final Context context = this.context != null ? this.context : this.service;
        if (context == null)
            return;

        try {
            final Intent extrasIntent = new Intent();
            extrasIntent.putExtra(BluetoothScannerProvider.EXTRA_ERROR_CODE, errorCode);
            callbackIntent.send(context, 0, extrasIntent);
        } catch (final PendingIntent.CanceledException e) {
            // Ignore
        }
    }
}
