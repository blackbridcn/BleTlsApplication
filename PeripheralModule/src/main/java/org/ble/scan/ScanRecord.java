package org.ble.scan;

import android.os.ParcelUuid;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: yuzzha
 * Date: 2021-11-12 12:16
 * Description:
 * Remark:
 */
public final class ScanRecord {

    private static final String TAG = "ScanRecord";

    // The following data type values are assigned by Bluetooth SIG.
    // For more details refer to Bluetooth 4.1 specification, Volume 3, Part C, Section 18.
    private static final int DATA_TYPE_FLAGS = 0x01;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07;

    private static final int DATA_TYPE_LOCAL_NAME_SHORT = 0x08;
    private static final int DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09;
    private static final int DATA_TYPE_TX_POWER_LEVEL = 0x0A;
    private static final int DATA_TYPE_SERVICE_DATA_16_BIT = 0x16;
    private static final int DATA_TYPE_SERVICE_DATA_32_BIT = 0x20;
    private static final int DATA_TYPE_SERVICE_DATA_128_BIT = 0x21;
    private static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF;

    // Flags of the advertising data.
    private final int advertiseFlags;

    @Nullable
    private final List<ParcelUuid> serviceUuids;

    @Nullable private final SparseArray<byte[]> manufacturerSpecificData;

    @Nullable private final Map<ParcelUuid, byte[]> serviceData;

    // Transmission power level(in dB).
    private final int txPowerLevel;

    // Local name of the Bluetooth LE device.
    private final String deviceName;

    // Raw bytes of scan record.
    private final byte[] bytes;

    private ScanRecord(@Nullable final List<ParcelUuid> serviceUuids,
                       @Nullable final SparseArray<byte[]> manufacturerData,
                       @Nullable final Map<ParcelUuid, byte[]> serviceData,
                       final int advertiseFlags, final int txPowerLevel,
                       final String localName, final byte[] bytes) {
        this.serviceUuids = serviceUuids;
        this.manufacturerSpecificData = manufacturerData;
        this.serviceData = serviceData;
        this.deviceName = localName;
        this.advertiseFlags = advertiseFlags;
        this.txPowerLevel = txPowerLevel;
        this.bytes = bytes;
    }

    /* package */ static ScanRecord parseFromBytes(@Nullable final byte[] scanRecord) {
        if (scanRecord == null) {
            return null;
        }

        int currentPos = 0;
        int advertiseFlag = -1;
        int txPowerLevel = Integer.MIN_VALUE;
        String localName = null;
        List<ParcelUuid> serviceUuids = null;
        SparseArray<byte[]> manufacturerData = null;
        Map<ParcelUuid, byte[]> serviceData = null;

        try {
            while (currentPos < scanRecord.length) {
                // length is unsigned int.
                final int length = scanRecord[currentPos++] & 0xFF;
                if (length == 0) {
                    break;
                }
                // Note the length includes the length of the field type itself.
                final int dataLength = length - 1;
                // fieldType is unsigned int.
                final int fieldType = scanRecord[currentPos++] & 0xFF;
                switch (fieldType) {
                    case DATA_TYPE_FLAGS:
                        advertiseFlag = scanRecord[currentPos] & 0xFF;
                        break;
                    case DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL:
                    case DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE:
                        if (serviceUuids == null)
                            serviceUuids = new ArrayList<>();
                        parseServiceUuid(scanRecord, currentPos,
                                dataLength, BluetoothUuid.UUID_BYTES_16_BIT, serviceUuids);
                        break;
                    case DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL:
                    case DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE:
                        if (serviceUuids == null)
                            serviceUuids = new ArrayList<>();
                        parseServiceUuid(scanRecord, currentPos, dataLength,
                                BluetoothUuid.UUID_BYTES_32_BIT, serviceUuids);
                        break;
                    case DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL:
                    case DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE:
                        if (serviceUuids == null)
                            serviceUuids = new ArrayList<>();
                        parseServiceUuid(scanRecord, currentPos, dataLength,
                                BluetoothUuid.UUID_BYTES_128_BIT, serviceUuids);
                        break;
                    case DATA_TYPE_LOCAL_NAME_SHORT:
                    case DATA_TYPE_LOCAL_NAME_COMPLETE:
                        localName = new String(
                                extractBytes(scanRecord, currentPos, dataLength));
                        break;
                    case DATA_TYPE_TX_POWER_LEVEL:
                        txPowerLevel = scanRecord[currentPos];
                        break;
                    case DATA_TYPE_SERVICE_DATA_16_BIT:
                    case DATA_TYPE_SERVICE_DATA_32_BIT:
                    case DATA_TYPE_SERVICE_DATA_128_BIT:
                        int serviceUuidLength = BluetoothUuid.UUID_BYTES_16_BIT;
                        if (fieldType == DATA_TYPE_SERVICE_DATA_32_BIT) {
                            serviceUuidLength = BluetoothUuid.UUID_BYTES_32_BIT;
                        } else if (fieldType == DATA_TYPE_SERVICE_DATA_128_BIT) {
                            serviceUuidLength = BluetoothUuid.UUID_BYTES_128_BIT;
                        }

                        final byte[] serviceDataUuidBytes = extractBytes(scanRecord, currentPos,
                                serviceUuidLength);
                        final ParcelUuid serviceDataUuid = BluetoothUuid.parseUuidFrom(
                                serviceDataUuidBytes);
                        final byte[] serviceDataArray = extractBytes(scanRecord,
                                currentPos + serviceUuidLength, dataLength - serviceUuidLength);
                        if (serviceData == null)
                            serviceData = new HashMap<>();
                        serviceData.put(serviceDataUuid, serviceDataArray);
                        break;
                    case DATA_TYPE_MANUFACTURER_SPECIFIC_DATA:
                        // The first two bytes of the manufacturer specific data are
                        // manufacturer ids in little endian.
                        final int manufacturerId = ((scanRecord[currentPos + 1] & 0xFF) << 8) +
                                (scanRecord[currentPos] & 0xFF);
                        final byte[] manufacturerDataBytes = extractBytes(scanRecord, currentPos + 2,
                                dataLength - 2);
                        if (manufacturerData == null)
                            manufacturerData = new SparseArray<>();
                        manufacturerData.put(manufacturerId, manufacturerDataBytes);
                        break;
                    default:
                        // Just ignore, we don't handle such data type.
                        break;
                }
                currentPos += dataLength;
            }

            return new ScanRecord(serviceUuids, manufacturerData, serviceData,
                    advertiseFlag, txPowerLevel, localName, scanRecord);
        } catch (final Exception e) {
            LogUtils.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
            // As the record is invalid, ignore all the parsed results for this packet
            // and return an empty record with raw scanRecord bytes in results
            return new ScanRecord(null, null, null,
                    -1, Integer.MIN_VALUE, null, scanRecord);
        }
    }


    //解析蓝牙 广播中的ServiceUUUID 中每个字节、bit中代表的含义
    private static int parseServiceUuid(@NonNull final byte[] scanRecord,
                                        int currentPos, int dataLength,
                                        final int uuidLength,
                                        @NonNull final List<ParcelUuid> serviceUuids) {
        while (dataLength > 0) {
            final byte[] uuidBytes = extractBytes(scanRecord, currentPos,
                    uuidLength);
            serviceUuids.add(BluetoothUuid.parseUuidFrom(uuidBytes));
            dataLength -= uuidLength;
            currentPos += uuidLength;
        }
        return currentPos;
    }

    // Helper method to extract bytes from byte array.
    private static byte[] extractBytes(@NonNull final byte[] scanRecord,
                                       final int start, final int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }


    public String getDeviceName() {
        return deviceName;
    }

    public int getAdvertiseFlags() {
        return advertiseFlags;
    }

    @Nullable
    public List<ParcelUuid> getServiceUuids() {
        return serviceUuids;
    }

    @Nullable
    public SparseArray<byte[]> getManufacturerSpecificData() {
        return manufacturerSpecificData;
    }

    @Nullable
    public byte[] getManufacturerSpecificData(final int manufacturerId) {
        if (manufacturerSpecificData == null) {
            return null;
        }
        return manufacturerSpecificData.get(manufacturerId);
    }

    @Nullable
    public Map<ParcelUuid, byte[]> getServiceData() {
        return serviceData;
    }

    @Nullable
    public byte[] getServiceData(@NonNull final ParcelUuid serviceDataUuid) {
        //noinspection ConstantConditions
        if (serviceDataUuid == null || serviceData == null) {
            return null;
        }
        return serviceData.get(serviceDataUuid);
    }

    public int getTxPowerLevel() {
        return txPowerLevel;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
