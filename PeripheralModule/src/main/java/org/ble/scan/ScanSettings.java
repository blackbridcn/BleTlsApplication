package org.ble.scan;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Author: yuzzha
 * Date: 2021-11-11 17:12
 * Description:
 * Remark:
 */
public final class ScanSettings implements Parcelable {

    /**
     * The default value of the maximum time for the device not to be discoverable before it will be
     * assumed lost.
     */
    public static final long MATCH_LOST_DEVICE_TIMEOUT_DEFAULT = 10000L; // [ms]

    /**
     * The default interval of the task that calls match lost events.
     */
    public static final long MATCH_LOST_TASK_INTERVAL_DEFAULT = 10000L; // [ms]

    /**
     * A special Bluetooth LE scan mode. Applications using this scan mode will passively listen for
     * other scan results without starting BLE scans themselves.
     * <p>
     * On Android Lollipop {@link #SCAN_MODE_LOW_POWER} will be used instead, as opportunistic
     * mode was not yet supported.
     * <p>
     * On pre-Lollipop devices it is possible to override the default intervals
     */
    public static final int SCAN_MODE_OPPORTUNISTIC = -1;
    // 优先省电模式 扫描优先
    public static final int SCAN_MODE_LOW_POWER = 0;
    // 平衡模式
    public static final int SCAN_MODE_BALANCED = 1;
    // 低延时  高功耗模式
    public static final int SCAN_MODE_LOW_LATENCY = 2;

    //回调model
    // 回调所有匹配的
    // 寻找符合过滤条件的蓝牙广播，如果没有设置过滤条件，则返回全部广播包
    public static final int CALLBACK_TYPE_ALL_MATCHES = 1;
    //回调第一次匹配成功的Device
    public static final int CALLBACK_TYPE_FIRST_MATCH = 2;
    //
    public static final int CALLBACK_TYPE_MATCH_LOST = 4;

    /**
     * Match one advertisement per filter
     */
    public static final int MATCH_NUM_ONE_ADVERTISEMENT = 1;
    public static final int MATCH_NUM_FEW_ADVERTISEMENT = 2;
    public static final int MATCH_NUM_MAX_ADVERTISEMENT = 3;

    //匹配规则
    public static final int MATCH_MODE_AGGRESSIVE = 1;
    //严格匹配
    public static final int MATCH_MODE_STICKY = 2;

    public static final int PHY_LE_ALL_SUPPORTED = 255;

    /**
     * Pre-Lollipop scanning requires a wakelock and the CPU cannot go to sleep.
     * To conserve power we can optionally scan for a certain duration (scan interval)
     * and then rest for a time before starting scanning again.
     * <p>
     * Android  API 5.0 之前(包括5.0) 在 Scan Bluetooth时，要求CPU不能休眠，
     * 这里采取的策略是:先扫描一段时间，再休眠一段时间，再重新开始搜索蓝牙，依次重复
     * powerSaveScanInterval 是指搜素时间段
     * powerSaveRestInterval 是中间休眠时间段
     * </p>
     */
    private final long powerSaveScanInterval;
    private final long powerSaveRestInterval;

    // Bluetooth LE scan mode.
    private final int scanMode;

    // Bluetooth LE scan callback type
    private final int callbackType;

    // Time of delay for reporting the scan result
    private final long reportDelayMillis;

    private final int numOfMatchesPerFilter;

    private final int matchMode;

    // Include only legacy advertising results
    //在Android 8.0时才有效
    private final boolean legacy;
    //在Android 8.0时才有效
    private final int phy;

    private final boolean useHardwareFilteringIfSupported;

    private final boolean useHardwareBatchingIfSupported;

    private boolean useHardwareCallbackTypesIfSupported;

    //Scan 超时
    private final long matchLostDeviceTimeout;

    private final long matchLostTaskInterval;


    private ScanSettings(final int scanMode, final int callbackType,
                         final long reportDelayMillis,
                         final int matchMode, final int numOfMatchesPerFilter,
                         final boolean legacy, final int phy,
                         final boolean hardwareFiltering,
                         final boolean hardwareBatching,
                         final boolean hardwareCallbackTypes,
                         final long matchTimeout, final long taskInterval,
                         final long powerSaveScanInterval, final long powerSaveRestInterval) {
        this.scanMode = scanMode;
        this.callbackType = callbackType;
        this.reportDelayMillis = reportDelayMillis;
        this.numOfMatchesPerFilter = numOfMatchesPerFilter;
        this.matchMode = matchMode;
        this.legacy = legacy;
        this.phy = phy;
        this.useHardwareFilteringIfSupported = hardwareFiltering;
        this.useHardwareBatchingIfSupported = hardwareBatching;
        this.useHardwareCallbackTypesIfSupported = hardwareCallbackTypes;
        this.matchLostDeviceTimeout = matchTimeout * 1000000L; // convert to nanos
        this.matchLostTaskInterval = taskInterval;
        this.powerSaveScanInterval = powerSaveScanInterval;
        this.powerSaveRestInterval = powerSaveRestInterval;
    }


    private ScanSettings(final Parcel in) {
        scanMode = in.readInt();
        callbackType = in.readInt();
        reportDelayMillis = in.readLong();
        matchMode = in.readInt();
        numOfMatchesPerFilter = in.readInt();
        legacy = in.readInt() != 0;
        phy = in.readInt();
        useHardwareFilteringIfSupported = in.readInt() == 1;
        useHardwareBatchingIfSupported = in.readInt() == 1;
        matchLostDeviceTimeout = in.readLong();
        matchLostTaskInterval = in.readLong();
        powerSaveScanInterval = in.readLong();
        powerSaveRestInterval = in.readLong();
    }

    public int getScanMode() {
        return scanMode;
    }

    public int getMatchMode() {
        return matchMode;
    }

    public int getNumOfMatches() {
        return numOfMatchesPerFilter;
    }

    public int getCallbackType() {
        return callbackType;
    }

    public long getMatchLostDeviceTimeout() {
        return matchLostDeviceTimeout;
    }

    public long getMatchLostTaskInterval() {
        return matchLostTaskInterval;
    }


    public long getPowerSaveRest() {
        return powerSaveRestInterval;
    }

    public long getPowerSaveScan() {
        return powerSaveScanInterval;
    }

    //5.0  之前是否开启、节电模式
    public boolean hasPowerSaveMode() {
        return powerSaveRestInterval > 0 && powerSaveScanInterval > 0;
    }

    /**
     * scan 到 BluetoothDevice 延时回调 时间
     * Returns report delay timestamp based on the device clock.
     */
    public long getReportDelayMillis() {
        return reportDelayMillis;
    }

    public boolean getUseHardwareBatchingIfSupported() {
        return useHardwareBatchingIfSupported;
    }

    public boolean getUseHardwareCallbackTypesIfSupported() {
        return useHardwareCallbackTypesIfSupported;
    }

    /**
     * Some devices with Android Marshmallow (Nexus 6) theoretically support other callback types,
     * but call {@link android.bluetooth.le.ScanCallback#onScanFailed(int)} with error = 5.
     * In that case the Scanner Compat will disable the hardware support and start using compat
     * mechanism.
     */
    /* package */ void disableUseHardwareCallbackTypes() {
        useHardwareCallbackTypesIfSupported = false;
    }

    public boolean getUseHardwareFilteringIfSupported() {
        return useHardwareFilteringIfSupported;
    }


    /**
     * Returns whether only legacy advertisements will be returned.
     * Legacy advertisements include advertisements as specified
     * by the Bluetooth core specification 4.2 and below.
     */
    public boolean getLegacy() {
        return legacy;
    }

    /**
     * Returns the physical layer used during a scan.
     */
    public int getPhy() {
        return phy;
    }


    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(scanMode);
        dest.writeInt(callbackType);
        dest.writeLong(reportDelayMillis);
        dest.writeInt(matchMode);
        dest.writeInt(numOfMatchesPerFilter);
        dest.writeInt(legacy ? 1 : 0);
        dest.writeInt(phy);
        dest.writeInt(useHardwareFilteringIfSupported ? 1 : 0);
        dest.writeInt(useHardwareBatchingIfSupported ? 1 : 0);
        dest.writeLong(matchLostDeviceTimeout);
        dest.writeLong(matchLostTaskInterval);
        dest.writeLong(powerSaveScanInterval);
        dest.writeLong(powerSaveRestInterval);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ScanSettings> CREATOR = new Creator<ScanSettings>() {
        @Override
        public ScanSettings[] newArray(final int size) {
            return new ScanSettings[size];
        }

        @Override
        public ScanSettings createFromParcel(final Parcel in) {
            return new ScanSettings(in);
        }
    };


    public static final class Builder {

        private int scanMode = SCAN_MODE_LOW_POWER;
        private int callbackType = CALLBACK_TYPE_ALL_MATCHES;
        private long reportDelayMillis = 0;
        private int matchMode = MATCH_MODE_AGGRESSIVE;
        private int numOfMatchesPerFilter = MATCH_NUM_MAX_ADVERTISEMENT;
        private boolean legacy = true;

        private int phy = PHY_LE_ALL_SUPPORTED;

        private boolean useHardwareFilteringIfSupported = true;
        private boolean useHardwareBatchingIfSupported = true;
        private boolean useHardwareCallbackTypesIfSupported = true;

        private long matchLostDeviceTimeout = MATCH_LOST_DEVICE_TIMEOUT_DEFAULT;
        private long matchLostTaskInterval = MATCH_LOST_TASK_INTERVAL_DEFAULT;

        private long powerSaveRestInterval = 0;
        private long powerSaveScanInterval = 0;


        public Builder setScanMode(int scanMode) {
            if (scanMode < SCAN_MODE_OPPORTUNISTIC || scanMode > SCAN_MODE_LOW_LATENCY) {
                throw new IllegalArgumentException("invalid scan mode " + scanMode);
            }
            this.scanMode = scanMode;
            return this;
        }

        public Builder setCallbackType(int callbackType) {
            if (!isValidCallbackType(callbackType)) {
                throw new IllegalArgumentException("invalid callback type - " + callbackType);
            }
            this.callbackType = callbackType;
            return this;
        }

        // Returns true if the callbackType is valid.
        private boolean isValidCallbackType(final int callbackType) {
            if (callbackType == CALLBACK_TYPE_ALL_MATCHES ||
                    callbackType == CALLBACK_TYPE_FIRST_MATCH ||
                    callbackType == CALLBACK_TYPE_MATCH_LOST) {
                return true;
            }
            return callbackType == (CALLBACK_TYPE_FIRST_MATCH | CALLBACK_TYPE_MATCH_LOST);
        }

        public Builder setReportDelayMillis(long reportDelayMillis) {
            if (reportDelayMillis < 0) {
                throw new IllegalArgumentException("reportDelay must be > 0");
            }
            this.reportDelayMillis = reportDelayMillis;
            return this;
        }

        public Builder setMatchMode(int matchMode) {
            if (matchMode < MATCH_MODE_AGGRESSIVE
                    || matchMode > MATCH_MODE_STICKY) {
                throw new IllegalArgumentException("invalid matchMode " + matchMode);
            }
            this.matchMode = matchMode;
            return this;
        }

        public Builder setNumOfMatches(int numOfMatches) {
            if (numOfMatches < MATCH_NUM_ONE_ADVERTISEMENT
                    || numOfMatches > MATCH_NUM_MAX_ADVERTISEMENT) {
                throw new IllegalArgumentException("invalid numOfMatches " + numOfMatches);
            }
            this.numOfMatchesPerFilter = numOfMatchesPerFilter;
            return this;
        }

        @NonNull
        public Builder setReportDelay(final long reportDelayMillis) {
            if (reportDelayMillis < 0) {
                throw new IllegalArgumentException("reportDelay must be > 0");
            }
            this.reportDelayMillis = reportDelayMillis;
            return this;
        }

        @NonNull
        public Builder setUseHardwareBatchingIfSupported(final boolean use) {
            useHardwareBatchingIfSupported = use;
            return this;
        }

        @NonNull
        public Builder setUseHardwareFilteringIfSupported(final boolean use) {
            useHardwareFilteringIfSupported = use;
            return this;
        }

        @NonNull
        public Builder setUseHardwareCallbackTypesIfSupported(final boolean use) {
            useHardwareCallbackTypesIfSupported = use;
            return this;
        }

        @NonNull
        public Builder setMatchOptions(final long deviceTimeoutMillis, final long taskIntervalMillis) {
            if (deviceTimeoutMillis <= 0 || taskIntervalMillis <= 0) {
                throw new IllegalArgumentException("maxDeviceAgeMillis and taskIntervalMillis must be > 0");
            }
            matchLostDeviceTimeout = deviceTimeoutMillis;
            matchLostTaskInterval = taskIntervalMillis;
            return this;
        }

        public Builder setMatchLostDeviceTimeout(final long deviceTimeoutMillis) {
            if (deviceTimeoutMillis <= 0) {
                throw new IllegalArgumentException("maxDeviceAgeMillis and taskIntervalMillis must be > 0");
            }
            matchLostDeviceTimeout = deviceTimeoutMillis;
            return this;
        }


        @NonNull
        public Builder setLegacy(final boolean legacy) {
            this.legacy = legacy;
            return this;
        }

        @NonNull
        public Builder setPhy(final int phy) {
            this.phy = phy;
            return this;
        }


        public ScanSettings build() {

            if (powerSaveRestInterval == 0 && powerSaveScanInterval == 0)
                updatePowerSaveSettings();

            return new ScanSettings(scanMode, callbackType, reportDelayMillis, matchMode,
                    numOfMatchesPerFilter, legacy, phy, useHardwareFilteringIfSupported,
                    useHardwareBatchingIfSupported, useHardwareCallbackTypesIfSupported,
                    // 默认Scan 10s 后超时停止扫描
                    matchLostDeviceTimeout,
                    // 默认再过十秒重新开始scan
                    matchLostTaskInterval,
                    powerSaveScanInterval, powerSaveRestInterval);
        }


        /**
         * Sets power save settings based on the scan mode selected.
         */
        private void updatePowerSaveSettings() {
            switch (scanMode) {
                case SCAN_MODE_LOW_LATENCY:
                    // Disable power save mode
                    powerSaveScanInterval = 0;
                    powerSaveRestInterval = 0;
                    break;
                case SCAN_MODE_BALANCED:
                    // Scan for 2 seconds every 5 seconds
                    powerSaveScanInterval = 2000;
                    powerSaveRestInterval = 3000;
                    break;
                case SCAN_MODE_OPPORTUNISTIC:
                    // It is not possible to emulate OPPORTUNISTIC scanning, but in theory
                    // that should be even less battery consuming than LOW_POWER.
                    // For pre-Lollipop devices intervals can be overwritten by
                    // setPowerSave(long, long) if needed.

                    // On Android Lollipop the native SCAN_MODE_LOW_POWER will be used instead
                    // of power save values.
                case SCAN_MODE_LOW_POWER:
                default:
                    // Scan for 0.5 second every 5 seconds
                    powerSaveScanInterval = 500;
                    powerSaveRestInterval = 4500;
                    break;
            }
        }
    }


}
