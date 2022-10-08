package org.ble.scan;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Author: yuzzha
 * Date: 2021-11-12 12:15
 * Description:
 * Remark:
 */
public final class ScanResult implements Parcelable {

    public static final int DATA_COMPLETE = 0x00;

    /**
     * For chained advertisements, indicates that the controller was
     * unable to receive all chained packets and the scan result contains
     * incomplete truncated data.
     */
    public static final int DATA_TRUNCATED = 0x02;

    /**
     * Indicates that the secondary physical layer was not used.
     */
    public static final int PHY_UNUSED = 0x00;

    /**
     * Advertising Set ID is not present in the packet.
     */
    public static final int SID_NOT_PRESENT = 0xFF;

    /**
     * TX power is not present in the packet.
     */
    public static final int TX_POWER_NOT_PRESENT = 0x7F;

    /**
     * Periodic advertising interval is not present in the packet.
     */
    public static final int PERIODIC_INTERVAL_NOT_PRESENT = 0x00;

    /**
     * Mask for checking whether event type represents legacy advertisement.
     */
    static final int ET_LEGACY_MASK = 0x10;

    /**
     * Mask for checking whether event type represents connectable advertisement.
     */
    static final int ET_CONNECTABLE_MASK = 0x01;


    // Remote Bluetooth device.
    @NonNull
    private final BluetoothDevice device;

    // Scan record, including advertising data and scan response data.
    @Nullable
    private ScanRecord scanRecord;

    // Received signal strength.
    private final int rssi;

    // Device timestamp when the result was last seen.
    private final long timestampNanos;

    private final int eventType;
    private final int primaryPhy;
    private final int secondaryPhy;
    private final int advertisingSid;
    private final int txPower;
    private final int periodicAdvertisingInterval;

    public ScanResult(@NonNull final BluetoothDevice device, final int eventType,
                      final int primaryPhy, final int secondaryPhy,
                      final int advertisingSid, final int txPower, final int rssi,
                      final int periodicAdvertisingInterval,
                      @Nullable final ScanRecord scanRecord, final long timestampNanos) {
        this.device = device;
        this.eventType = eventType;
        this.primaryPhy = primaryPhy;
        this.secondaryPhy = secondaryPhy;
        this.advertisingSid = advertisingSid;
        this.txPower = txPower;
        this.rssi = rssi;
        this.periodicAdvertisingInterval = periodicAdvertisingInterval;
        this.scanRecord = scanRecord;
        this.timestampNanos = timestampNanos;
    }

    public ScanResult(@NonNull final BluetoothDevice device, @Nullable final ScanRecord scanRecord,
                      int rssi, long timestampNanos) {
        this.device = device;
        this.scanRecord = scanRecord;
        this.rssi = rssi;
        this.timestampNanos = timestampNanos;
        this.eventType = (DATA_COMPLETE << 5) | ET_LEGACY_MASK | ET_CONNECTABLE_MASK;
        this.primaryPhy = 1; // BluetoothDevice.PHY_LE_1M;
        this.secondaryPhy = PHY_UNUSED;
        this.advertisingSid = SID_NOT_PRESENT;
        this.txPower = 127;
        this.periodicAdvertisingInterval = 0;
    }

    private ScanResult(final Parcel in) {
        device = BluetoothDevice.CREATOR.createFromParcel(in);
        if (in.readInt() == 1) {
            scanRecord = ScanRecord.parseFromBytes(in.createByteArray());
        }
        rssi = in.readInt();
        timestampNanos = in.readLong();
        eventType = in.readInt();
        primaryPhy = in.readInt();
        secondaryPhy = in.readInt();
        advertisingSid = in.readInt();
        txPower = in.readInt();
        periodicAdvertisingInterval = in.readInt();
    }
    @NonNull
    public BluetoothDevice getDevice() {
        return device;
    }

    @Nullable
    public ScanRecord getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(@Nullable ScanRecord scanRecord) {
        this.scanRecord = scanRecord;
    }

    public int getRssi() {
        return rssi;
    }

    public long getTimestampNanos() {
        return timestampNanos;
    }

    public int getEventType() {
        return eventType;
    }

    public int getPrimaryPhy() {
        return primaryPhy;
    }

    public int getSecondaryPhy() {
        return secondaryPhy;
    }

    public int getAdvertisingSid() {
        return advertisingSid;
    }

    public int getTxPower() {
        return txPower;
    }

    public int getPeriodicAdvertisingInterval() {
        return periodicAdvertisingInterval;
    }



    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        device.writeToParcel(dest, flags);
        if (scanRecord != null) {
            dest.writeInt(1);
            dest.writeByteArray(scanRecord.getBytes());
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(rssi);
        dest.writeLong(timestampNanos);
        dest.writeInt(eventType);
        dest.writeInt(primaryPhy);
        dest.writeInt(secondaryPhy);
        dest.writeInt(advertisingSid);
        dest.writeInt(txPower);
        dest.writeInt(periodicAdvertisingInterval);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public int hashCode() {
        return Objects.hash(device, rssi, scanRecord, timestampNanos,
                eventType, primaryPhy, secondaryPhy,
                advertisingSid, txPower,
                periodicAdvertisingInterval);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ScanResult other = (ScanResult) obj;
        return Objects.equals(device, other.device) && (rssi == other.rssi) &&
                Objects.equals(scanRecord, other.scanRecord) &&
                (timestampNanos == other.timestampNanos) &&
                eventType == other.eventType &&
                primaryPhy == other.primaryPhy &&
                secondaryPhy == other.secondaryPhy &&
                advertisingSid == other.advertisingSid &&
                txPower == other.txPower &&
                periodicAdvertisingInterval == other.periodicAdvertisingInterval;
    }

    @Override
    public String toString() {
        return "ScanResult{" + "device=" + device + ", scanRecord=" +
                Objects.toString(scanRecord) + ", rssi=" + rssi +
                ", timestampNanos=" + timestampNanos + ", eventType=" + eventType +
                ", primaryPhy=" + primaryPhy + ", secondaryPhy=" + secondaryPhy +
                ", advertisingSid=" + advertisingSid + ", txPower=" + txPower +
                ", periodicAdvertisingInterval=" + periodicAdvertisingInterval + '}';
    }
    public static final Creator<ScanResult> CREATOR =new Creator<ScanResult>() {
        @Override
        public ScanResult createFromParcel(Parcel source) {
            return new ScanResult(source);
        }

        @Override
        public ScanResult[] newArray(int size) {
            return new ScanResult[size];
        }
    };
}
