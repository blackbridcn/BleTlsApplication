package org.ble.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Author: yuzzha
 * Date: 2021-11-12 11:48
 * Description:
 * Remark:
 */
public final class ScanFilter implements Parcelable {

    @Nullable
    private final String deviceName;
    @Nullable
    private final String deviceAddress;

    @Nullable
    private final ParcelUuid serviceUuid;
    @Nullable
    private final ParcelUuid serviceUuidMask;

    @Nullable
    private final ParcelUuid serviceDataUuid;
    @Nullable
    private final byte[] serviceData;
    @Nullable
    private final byte[] serviceDataMask;

    private final int manufacturerId;
    @Nullable
    private final byte[] manufacturerData;
    @Nullable
    private final byte[] manufacturerDataMask;

    private static final ScanFilter EMPTY = new Builder().build();

    private ScanFilter(@Nullable final String name, @Nullable final String deviceAddress,
                       @Nullable final ParcelUuid uuid, @Nullable final ParcelUuid uuidMask,
                       @Nullable final ParcelUuid serviceDataUuid, @Nullable final byte[] serviceData,
                       @Nullable final byte[] serviceDataMask, final int manufacturerId,
                       @Nullable final byte[] manufacturerData,
                       @Nullable final byte[] manufacturerDataMask) {
        this.deviceName = name;
        this.serviceUuid = uuid;
        this.serviceUuidMask = uuidMask;
        this.deviceAddress = deviceAddress;
        this.serviceDataUuid = serviceDataUuid;
        this.serviceData = serviceData;
        this.serviceDataMask = serviceDataMask;
        this.manufacturerId = manufacturerId;
        this.manufacturerData = manufacturerData;
        this.manufacturerDataMask = manufacturerDataMask;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(deviceName == null ? 0 : 1);
        if (deviceName != null) {
            dest.writeString(deviceName);
        }
        dest.writeInt(deviceAddress == null ? 0 : 1);
        if (deviceAddress != null) {
            dest.writeString(deviceAddress);
        }
        dest.writeInt(serviceUuid == null ? 0 : 1);
        if (serviceUuid != null) {
            dest.writeParcelable(serviceUuid, flags);
            dest.writeInt(serviceUuidMask == null ? 0 : 1);
            if (serviceUuidMask != null) {
                dest.writeParcelable(serviceUuidMask, flags);
            }
        }
        dest.writeInt(serviceDataUuid == null ? 0 : 1);
        if (serviceDataUuid != null) {
            dest.writeParcelable(serviceDataUuid, flags);
            dest.writeInt(serviceData == null ? 0 : 1);
            if (serviceData != null) {
                dest.writeInt(serviceData.length);
                dest.writeByteArray(serviceData);

                dest.writeInt(serviceDataMask == null ? 0 : 1);
                if (serviceDataMask != null) {
                    dest.writeInt(serviceDataMask.length);
                    dest.writeByteArray(serviceDataMask);
                }
            }
        }
        dest.writeInt(manufacturerId);
        dest.writeInt(manufacturerData == null ? 0 : 1);
        if (manufacturerData != null) {
            dest.writeInt(manufacturerData.length);
            dest.writeByteArray(manufacturerData);

            dest.writeInt(manufacturerDataMask == null ? 0 : 1);
            if (manufacturerDataMask != null) {
                dest.writeInt(manufacturerDataMask.length);
                dest.writeByteArray(manufacturerDataMask);
            }
        }
    }

    /**
     * A {@link Creator} to create {@link ScanFilter} from parcel.
     */
    public static final Creator<ScanFilter> CREATOR = new Creator<ScanFilter>() {

        @Override
        public ScanFilter[] newArray(final int size) {
            return new ScanFilter[size];
        }

        @Override
        public ScanFilter createFromParcel(final Parcel in) {
            final Builder builder = new Builder();
            if (in.readInt() == 1) {
                builder.setDeviceName(in.readString());
            }
            if (in.readInt() == 1) {
                builder.setDeviceAddress(in.readString());
            }
            if (in.readInt() == 1) {
                ParcelUuid uuid = in.readParcelable(ParcelUuid.class.getClassLoader());
                builder.setServiceUuid(uuid);
                if (in.readInt() == 1) {
                    ParcelUuid uuidMask = in.readParcelable(
                            ParcelUuid.class.getClassLoader());
                    builder.setServiceUuid(uuid, uuidMask);
                }
            }
            if (in.readInt() == 1) {
                ParcelUuid serviceDataUuid = in.readParcelable(ParcelUuid.class.getClassLoader());
                if (in.readInt() == 1) {
                    final int serviceDataLength = in.readInt();
                    final byte[] serviceData = new byte[serviceDataLength];
                    in.readByteArray(serviceData);
                    if (in.readInt() == 0) {
                        //noinspection ConstantConditions
                        builder.setServiceData(serviceDataUuid, serviceData);
                    } else {
                        final int serviceDataMaskLength = in.readInt();
                        final byte[] serviceDataMask = new byte[serviceDataMaskLength];
                        in.readByteArray(serviceDataMask);
                        //noinspection ConstantConditions
                        builder.setServiceData(serviceDataUuid, serviceData, serviceDataMask);
                    }
                }
            }

            final int manufacturerId = in.readInt();
            if (in.readInt() == 1) {
                final int manufacturerDataLength = in.readInt();
                final byte[] manufacturerData = new byte[manufacturerDataLength];
                in.readByteArray(manufacturerData);
                if (in.readInt() == 0) {
                    builder.setManufacturerData(manufacturerId, manufacturerData);
                } else {
                    final int manufacturerDataMaskLength = in.readInt();
                    final byte[] manufacturerDataMask = new byte[manufacturerDataMaskLength];
                    in.readByteArray(manufacturerDataMask);
                    builder.setManufacturerData(manufacturerId, manufacturerData,
                            manufacturerDataMask);
                }
            }

            return builder.build();
        }
    };

    boolean isAllFieldsEmpty() {
        return EMPTY.equals(this);
    }

    /**
     * Returns the filter set the device name field of Bluetooth advertisement data.
     */
    @Nullable
    public String getDeviceName() {
        return deviceName;
    }


    @Nullable
    public ParcelUuid getServiceUuid() {
        return serviceUuid;
    }

    @Nullable
    public ParcelUuid getServiceUuidMask() {
        return serviceUuidMask;
    }

    @Nullable
    public String getDeviceAddress() {
        return deviceAddress;
    }


    @Nullable
    public byte[] getServiceData() {
        return serviceData;
    }

    @Nullable
    public byte[] getServiceDataMask() {
        return serviceDataMask;
    }

    @Nullable
    public ParcelUuid getServiceDataUuid() {
        return serviceDataUuid;
    }

    /**
     * Returns the manufacturer id. -1 if the manufacturer filter is not set.
     */
    public int getManufacturerId() {
        return manufacturerId;
    }

    @Nullable
    public byte[] getManufacturerData() {
        return manufacturerData;
    }

    @Nullable
    public byte[] getManufacturerDataMask() {
        return manufacturerDataMask;
    }

    public boolean matches(@Nullable final ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        final BluetoothDevice device = scanResult.getDevice();
        // Device match.
        if (deviceAddress != null && !deviceAddress.equals(device.getAddress())) {
            return false;
        }

        final ScanRecord scanRecord = scanResult.getScanRecord();

        // Scan record is null but there exist filters on it.
        if (scanRecord == null
                && (deviceName != null || serviceUuid != null || manufacturerData != null
                || serviceData != null)) {
            return false;
        }

        // Local name match.
        if (deviceName != null && !deviceName.equals(scanRecord.getDeviceName())) {
            return false;
        }

        // UUID match.
        if (serviceUuid != null && !matchesServiceUuids(serviceUuid, serviceUuidMask,
                scanRecord.getServiceUuids())) {
            return false;
        }

        // Service data match
        if (serviceDataUuid != null && scanRecord != null) {
            if (!matchesPartialData(serviceData, serviceDataMask,
                    scanRecord.getServiceData(serviceDataUuid))) {
                return false;
            }
        }

        // Manufacturer data match.
        if (manufacturerId >= 0 && scanRecord != null) {
            //noinspection RedundantIfStatement
            if (!matchesPartialData(manufacturerData, manufacturerDataMask,
                    scanRecord.getManufacturerSpecificData(manufacturerId))) {
                return false;
            }
        }
        // All filters match.
        return true;
    }

    private static boolean matchesServiceUuids(@Nullable final ParcelUuid uuid,
                                               @Nullable final ParcelUuid parcelUuidMask,
                                               @Nullable final List<ParcelUuid> uuids) {
        if (uuid == null) {
            return true;
        }
        if (uuids == null) {
            return false;
        }

        for (final ParcelUuid parcelUuid : uuids) {
            final UUID uuidMask = parcelUuidMask == null ? null : parcelUuidMask.getUuid();
            if (matchesServiceUuid(uuid.getUuid(), uuidMask, parcelUuid.getUuid())) {
                return true;
            }
        }
        return false;
    }

    // Check if the uuid pattern matches the particular service uuid.
    private static boolean matchesServiceUuid(@NonNull  final UUID uuid,
                                              @Nullable final UUID mask,
                                              @NonNull  final UUID data) {
        if (mask == null) {
            return uuid.equals(data);
        }
        if ((uuid.getLeastSignificantBits() & mask.getLeastSignificantBits()) !=
                (data.getLeastSignificantBits() & mask.getLeastSignificantBits())) {
            return false;
        }
        return ((uuid.getMostSignificantBits() & mask.getMostSignificantBits()) ==
                (data.getMostSignificantBits() & mask.getMostSignificantBits()));
    }

    // Check whether the data pattern matches the parsed data.
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean matchesPartialData(@Nullable final byte[] data,
                                       @Nullable final byte[] dataMask,
                                       @Nullable final byte[] parsedData) {
        if (data == null) {
            // If filter data is null it means it doesn't matter.
            // We return true if any data matching the manufacturerId were found.
            return parsedData != null;
        }
        if (parsedData == null || parsedData.length < data.length) {
            return false;
        }
        if (dataMask == null) {
            for (int i = 0; i < data.length; ++i) {
                if (parsedData[i] != data[i]) {
                    return false;
                }
            }
            return true;
        }
        for (int i = 0; i < data.length; ++i) {
            if ((dataMask[i] & parsedData[i]) != (dataMask[i] & data[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "BluetoothLeScanFilter [deviceName=" + deviceName + ", deviceAddress="
                + deviceAddress
                + ", mUuid=" + serviceUuid + ", uuidMask=" + serviceUuidMask
                + ", serviceDataUuid=" + Objects.toString(serviceDataUuid) + ", serviceData="
                + Arrays.toString(serviceData) + ", serviceDataMask="
                + Arrays.toString(serviceDataMask) + ", manufacturerId=" + manufacturerId
                + ", manufacturerData=" + Arrays.toString(manufacturerData)
                + ", manufacturerDataMask=" + Arrays.toString(manufacturerDataMask) + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceName, deviceAddress, manufacturerId,
                Arrays.hashCode(manufacturerData),
                Arrays.hashCode(manufacturerDataMask),
                serviceDataUuid,
                Arrays.hashCode(serviceData),
                Arrays.hashCode(serviceDataMask),
                serviceUuid, serviceUuidMask);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ScanFilter other = (ScanFilter) obj;
        return Objects.equals(deviceName, other.deviceName) &&
                Objects.equals(deviceAddress, other.deviceAddress) &&
                manufacturerId == other.manufacturerId &&
                Objects.deepEquals(manufacturerData, other.manufacturerData) &&
                Objects.deepEquals(manufacturerDataMask, other.manufacturerDataMask) &&
                Objects.equals(serviceDataUuid, other.serviceDataUuid) &&
                Objects.deepEquals(serviceData, other.serviceData) &&
                Objects.deepEquals(serviceDataMask, other.serviceDataMask) &&
                Objects.equals(serviceUuid, other.serviceUuid) &&
                Objects.equals(serviceUuidMask, other.serviceUuidMask);
    }


    public static final class Builder {
        private String deviceName;
        private String deviceAddress;

        private ParcelUuid serviceUuid;
        private ParcelUuid uuidMask;

        private ParcelUuid serviceDataUuid;
        private byte[] serviceData;
        private byte[] serviceDataMask;

        private int manufacturerId = -1;
        private byte[] manufacturerData;
        private byte[] manufacturerDataMask;

        public Builder setDeviceName(@Nullable final String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public Builder setDeviceAddress(@Nullable final String deviceAddress) {
            if (deviceAddress != null && !BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
                throw new IllegalArgumentException("invalid device address " + deviceAddress);
            }
            this.deviceAddress = deviceAddress;
            return this;
        }

        public Builder setServiceUuid(@Nullable final ParcelUuid serviceUuid) {
            this.serviceUuid = serviceUuid;
            this.uuidMask = null; // clear uuid mask
            return this;
        }

        public Builder setServiceUuid(@Nullable final ParcelUuid serviceUuid,
                                      @Nullable final ParcelUuid uuidMask) {
            if (uuidMask != null && serviceUuid == null) {
                throw new IllegalArgumentException("uuid is null while uuidMask is not null!");
            }
            this.serviceUuid = serviceUuid;
            this.uuidMask = uuidMask;
            return this;
        }

        public Builder setServiceData(@NonNull final ParcelUuid serviceDataUuid,
                                      @Nullable final byte[] serviceData) {
            //noinspection ConstantConditions
            if (serviceDataUuid == null) {
                throw new IllegalArgumentException("serviceDataUuid is null!");
            }
            this.serviceDataUuid = serviceDataUuid;
            this.serviceData = serviceData;
            this.serviceDataMask = null; // clear service data mask
            return this;
        }

        public Builder setServiceData(@NonNull final ParcelUuid serviceDataUuid,
                                      @Nullable final byte[] serviceData,
                                      @Nullable final byte[] serviceDataMask) {
            //noinspection ConstantConditions
            if (serviceDataUuid == null) {
                throw new IllegalArgumentException("serviceDataUuid is null");
            }
            if (serviceDataMask != null) {
                if (serviceData == null) {
                    throw new IllegalArgumentException(
                            "serviceData is null while serviceDataMask is not null");
                }
                // Since the serviceDataMask is a bit mask for serviceData, the lengths of the two
                // byte array need to be the same.
                if (serviceData.length != serviceDataMask.length) {
                    throw new IllegalArgumentException(
                            "size mismatch for service data and service data mask");
                }
            }
            this.serviceDataUuid = serviceDataUuid;
            this.serviceData = serviceData;
            this.serviceDataMask = serviceDataMask;
            return this;
        }

        public Builder setManufacturerData(final int manufacturerId,
                                           @Nullable final byte[] manufacturerData) {
            if (manufacturerData != null && manufacturerId < 0) {
                throw new IllegalArgumentException("invalid manufacture id");
            }
            this.manufacturerId = manufacturerId;
            this.manufacturerData = manufacturerData;
            this.manufacturerDataMask = null; // clear manufacturer data mask
            return this;
        }

        public Builder setManufacturerData(final int manufacturerId,
                                           @Nullable final byte[] manufacturerData,
                                           @Nullable final byte[] manufacturerDataMask) {
            if (manufacturerData != null && manufacturerId < 0) {
                throw new IllegalArgumentException("invalid manufacture id");
            }
            if (manufacturerDataMask != null) {
                if (manufacturerData == null) {
                    throw new IllegalArgumentException(
                            "manufacturerData is null while manufacturerDataMask is not null");
                }
                // Since the manufacturerDataMask is a bit mask for manufacturerData, the lengths
                // of the two byte array need to be the same.
                if (manufacturerData.length != manufacturerDataMask.length) {
                    throw new IllegalArgumentException(
                            "size mismatch for manufacturerData and manufacturerDataMask");
                }
            }
            this.manufacturerId = manufacturerId;
            this.manufacturerData = manufacturerData;
            this.manufacturerDataMask = manufacturerDataMask;
            return this;
        }


        public ScanFilter build() {
            return new ScanFilter(deviceName, deviceAddress, serviceUuid, uuidMask,
                    serviceDataUuid, serviceData, serviceDataMask,
                    manufacturerId, manufacturerData, manufacturerDataMask);
        }

    }
}
