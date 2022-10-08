package org.ble.core.peripheral;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.ble.callback.BleGattServerCallback;
import org.ble.callback.ClientReqCallback;
import org.ble.core.BleCoreImpl;
import org.ble.utils.BleBoothHelp;
import org.e.ble.utils.HexStrUtils;

import org.jetbrains.annotations.NotNull;
import org.utils.ByteHexUtils;
import org.utils.LogUtils;
import org.utils.StringUtils;

import java.util.UUID;

/**
 * Author: yuzzha
 * Date: 2021-10-14 13:49
 * Description:
 * Remark:
 */
public class PeripheralImpl implements PeripheralCore {

    private static String TAG = PeripheralImpl.class.getSimpleName();

    private Context mContext;

    BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothGattServer mBluetoothGattServer;

    private int maxMtu = 20;

    private boolean hasConnClient;
    private BluetoothDevice clientDevice;


    private BluetoothGattCharacteristic txCharacteristic;
    private String serviceUuid;
    private String rxCharUuid = "";
    private String txCharUuid = "";
    private String mCCCDUuid = "";
    byte[] cccdValue = null;


    private ClientReqCallback clientReqCallback;

    BleGattServerCallback bleGattServerCallback;
    BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private int clientMsgCount = 0;
    private int clientMsgIndex = 0;
    private byte[][] clientPackage;

    private byte[][] msgPackage;
    private int count;
    int index;


    public PeripheralImpl(@NotNull  BluetoothManager bluetoothManager, Context mContext) {
        this.mContext = mContext.getApplicationContext();
        this.bluetoothManager = bluetoothManager;
        this.bluetoothAdapter=bluetoothManager.getAdapter();
        this.mBluetoothLeAdvertiser = this.bluetoothManager.getAdapter().getBluetoothLeAdvertiser();

        if (this.bluetoothAdapter != null && bluetoothAdapter.isMultipleAdvertisementSupported()) {

        } else {
            LogUtils.i(TAG, "this phone not supported ble advertisement ");
        }
        LogUtils.i(TAG, "peripheral init ");
    }


    @Override
    public void startAdvertising(
            @NonNull AdvertiseSettings settings,
            @NonNull AdvertiseData advertiseData,
            @NonNull AdvertiseCallback callback) {
        this.mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, callback);
    }

    @Override
    public void startAdvertising(
            @NonNull String serviceUUID,
            @NonNull AdvertiseCallback callback) {
        this.mBluetoothLeAdvertiser.startAdvertising(
                buildAdvertiseSettings(),
                buildAdvertiseData(UUID.fromString(serviceUUID)),
                buildScanResponData(UUID.fromString(serviceUUID)),
                callback);
    }

    @Override
    public void startAdvertising(
            @NonNull byte[] serviceUUID,
            @NonNull AdvertiseCallback callback) {
        this.mBluetoothLeAdvertiser.startAdvertising(
                buildAdvertiseSettings(),
                buildAdvertiseData(UUID.nameUUIDFromBytes(serviceUUID)),
                buildScanResponData(UUID.nameUUIDFromBytes(serviceUUID)),
                callback);
    }

    @Override
    public void stopAdvertising(
            @NonNull AdvertiseCallback callback) {
        if (this.mBluetoothLeAdvertiser != null) {
            LogUtils.e(TAG, "stopAdvertising ");
            this.mBluetoothLeAdvertiser.stopAdvertising(callback);
        }
    }


    //广播设置(必须)
    private AdvertiseSettings buildAdvertiseSettings() {
        return new AdvertiseSettings.Builder()
                //设置广播模式，以控制广播的功率和延迟。 ADVERTISE_MODE_LOW_LATENCY为高功率，低延迟
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                //设置蓝牙广播发射功率级别
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                //广播时限。 0为不限制
                .setTimeout(0)
                //设置广告类型是可连接还是不可连接
                .setConnectable(true)
                .build();
    }

    //广播包（Advertising Data）
    //广播包是每个外设都必须广播的，而响应包是可选的。
    //每个广播包的长度必须是31个字节，如果不到31个字节 ，则剩下的全用0填充 补全
    //广播数据单元 = 长度值Length + AD type + AD Data
    private AdvertiseData buildAdvertiseData(UUID serviceUuid) {
        return new AdvertiseData.Builder()
                //广播包中是否包含设备名称
                .setIncludeDeviceName(false)
                //设置广播包中是否包含发射功率
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(new ParcelUuid(serviceUuid))
                //设备厂商自定义数据，将其转化为字节数组传入
                .addManufacturerData(0x4C, new byte[]{})
                .build();
    }

    private AdvertiseData buildScanResponData(UUID serviceUuid) {
        return new AdvertiseData.Builder()
                //隐藏广播设备名称
                .setIncludeDeviceName(true)
                //隐藏发射功率级别
                .setIncludeDeviceName(true)
                //.addServiceData(new ParcelUuid(serviceUuid), new byte[]{})
                //设备厂商自定义数据，将其转化为字节数组传入
                .addManufacturerData(0x4C, new byte[]{})
                .build();
    }

    @Override
    public void startGattServer(
            @NonNull String serviceUuid,
            @NonNull String rxCharUuid,
            @NonNull String txCharUuid,
            @NonNull String descUuid) {
        openGattServer(serviceUuid,
                rxCharUuid,
                txCharUuid,
                descUuid);
    }

    @Override
    public void startGattServer(
            @NonNull String serviceUuid,
            @NonNull String rxCharUuid,
            @NonNull String txCharUuid,
            @NonNull String descUuid,
            @Nullable BleGattServerCallback serverCallback) {
        setGattServerCallback(serverCallback);
        openGattServer(serviceUuid,
                rxCharUuid,
                txCharUuid,
                descUuid);
    }

    @Override
    public void setGattServerCallback(@NonNull BleGattServerCallback serverCallback) {
        this.bleGattServerCallback = serverCallback;
    }

    public void openGattServer(String serviceUuid, String rxCharUuid, String
            txCharUuid, String mCCCDUuid) {
        cccdValue = new byte[2];
        BluetoothGattService gattService = null;
        UUID service = UUID.fromString(serviceUuid);

        gattService = new BluetoothGattService(service, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //只读的特征值 给客户端用来写的
        BluetoothGattCharacteristic rxCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(rxCharUuid),
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                //BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        this.rxCharUuid = rxCharUuid;

        gattService.addCharacteristic(rxCharacteristic);

        // 客户端用来接收数据的，客户端需要进行 notify的
        BluetoothGattCharacteristic txCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(txCharUuid),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);
        this.txCharUuid = txCharUuid;

        //客户端特性配置描述符(Client Characteristic Configuration Descriptor，CCCD)
        BluetoothGattDescriptor mCCCD = new BluetoothGattDescriptor(
                UUID.fromString(mCCCDUuid),
                BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);
        mCCCD.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        this.mCCCDUuid = mCCCDUuid;
        txCharacteristic.addDescriptor(mCCCD);
        this.txCharacteristic = txCharacteristic;
        gattService.addCharacteristic(txCharacteristic);
        mBluetoothGattServer = bluetoothManager.openGattServer(this.mContext, gattServerCallback);
        try {
            //boolean hasGattServer =
            mBluetoothGattServer.addService(gattService);
        } catch (Exception e) {
            LogUtils.e(TAG, "BluetoothGattServer addService  Exception :" + e.getMessage());
        }
        LogUtils.e(TAG,
                String.format(" startGattServer \n serviceUuid = %s,\n rxCharUuid = %s,\n txCharUuid =%s,\n mCCCDUuid= %s",
                        serviceUuid, rxCharUuid, txCharUuid, mCCCDUuid));
    }

    //周边
    private BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            LogUtils.e(TAG, "onConnectionStateChange status :" + status + "  newState :" + newState);
            //long startTime = System.currentTimeMillis();
            if (!hasConnClient && newState == BluetoothProfile.STATE_CONNECTED) {
                if (StringUtils.isNotEmpty(device.getName()) /*&& device.getName().startsWith("Mi Smart Band")*/) {
                    //smartBrandTime=startTime;
                    // hasBrands=true;
                    LogUtils.e(TAG, "-------> 连接到手环 Address :" + device.getAddress() + " Name:" + device.getName());
                    disConnGattClient(device);
                } else {
                    // hasBrands=false;
                    LogUtils.e(TAG, "gattServerCallback 连接成功 Address :" + device.getAddress() + " Name:" + device.getName());
                    clientDevice = device;
                    hasConnClient = true;
                    if (bleGattServerCallback != null) {
                        bleGattServerCallback.onConnBleDevice(device);
                    }
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (StringUtils.isNotEmpty(device.getName())) {
                    LogUtils.e(TAG, "lastLink 手环  Address :" + device.getName());
                } else if (hasConnClient) {
                    LogUtils.e(TAG, "断开连接  currLink:" + device.getAddress());
                    if (TextUtils.equals(clientDevice.getAddress(), device.getAddress())) {
                        hasConnClient = false;
                        clientDevice = null;
                        if (bleGattServerCallback != null) {
                            bleGattServerCallback.onDisConnBleDevice(device);
                        }
                    }
                }
            }
        }

        //添加本地服务回调
        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtils.e(TAG, "添加Gatt服务成功 UUUID =  " + service.getUuid());
                if (bleGattServerCallback != null) {
                    bleGattServerCallback.onServiceStarted();
                }
            } else {
                LogUtils.e(TAG, "添加Gatt服务失败");
            }
        }

        //响应客户端 读取 特征值 回调函數
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            //A remote client has requested to read a local characteristic.
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());

            LogUtils.e(TAG, " onCharacteristicReadRequest \n" +
                    " UUID: " + characteristic.getUuid().toString() + "\n" +
                    " txUUID :" + txCharUuid);
        }

        //响应客户端 写入 特征值 回调函數
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            //A remote client has requested to write to a local characteristic.
            //刷新该特征值
            characteristic.setValue(value);
            //响应客户端
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            LogUtils.e(TAG, " onCharacteristicWriteRequest \n" +
                    " Value: " + ByteHexUtils.INSTANCE.byteArrayToHexString(value) + "\n" +
                    " UUID: " + characteristic.getUuid().toString()
            );
            //这里是Rx characteristic
            if (bleGattServerCallback != null) {
                bleGattServerCallback.onClientBleReq(device, characteristic, value, maxMtu);
            }
            if (clientReqCallback != null) {
                clientReqCallback.onClientResp(device, value);
            }
        }

        //响应客户端 读取 描述 回调函數
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            //A remote client has requested to read a local descriptor.
            BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
            String descUuid = descriptor.getUuid().toString();

            if (TextUtils.equals(descUuid, mCCCDUuid)
                    && characteristic != null
                    && TextUtils.equals(characteristic.getUuid().toString(), txCharUuid)
            ) {
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, cccdValue);
                LogUtils.e(TAG, " --- > onDescriptorReadRequest GATT_SUCCESS " + ByteHexUtils.INSTANCE.byteArrayToHexString(cccdValue));
            } else {
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, null);
                LogUtils.e(TAG, " --- > onDescriptorReadRequest GATT_FAILURE ");
            }
        }

        //响应客户端 写入 描述 回调函數
        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor,
                                             boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            //A remote client has requested to write to a local descriptor.
            BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
            String descUuid = descriptor.getUuid().toString();
            if (TextUtils.equals(mCCCDUuid, descUuid)
                    && value.length == 2
                    && characteristic != null
                    && TextUtils.equals(characteristic.getUuid().toString(), txCharUuid)
            ) {
                descriptor.setValue(value);
                txCharacteristic = characteristic;
                System.arraycopy(value, 0, cccdValue, 0, 2);
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
                LogUtils.e(TAG, " ----> onDescriptorWriteRequest \n" +
                        " BluetoothGatt.GATT_SUCCESS \n" +
                        " Value: " + ByteHexUtils.INSTANCE.byteArrayToHexString(value) + "\n" +
                        " Thread :" + Thread.currentThread().getName() + "\n" +
                        " mCCCD Value:" + ByteHexUtils.INSTANCE.byteArrayToHexString(value));
                if (bleGattServerCallback != null) {
                    // bleGattServerCallback.onDescriptorReadRequest();
                }
            } else {
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, null);
                LogUtils.e(TAG, "----> onDescriptorWriteRequest \n " +
                        " descriptor UUID :" + descriptor.getUuid().toString() + "\n " +
                        " characteristic :" + characteristic + " \n" +
                        " BluetoothGatt.GATT_FAILURE");
            }
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtils.e(TAG, device.getAddress() + " 通知发送成功");
                ++clientMsgCount;
                if (clientMsgCount < clientPackage.length) {
                    txCharacteristic.setValue(clientPackage[clientMsgCount]);
                    boolean notify = mBluetoothGattServer.notifyCharacteristicChanged(clientDevice, txCharacteristic, false);
                }
            } else {
                //closeGattServer();
                LogUtils.e(TAG, device.getAddress() + " 通知发送失败 status = " + status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
            if (mtu > 23) {
                maxMtu = mtu - 3;
            }
            LogUtils.e(TAG, "onMtuChanged mtu :" + mtu);
        }

        //interval：连接间隔（connection intervals ），范围在 7.5 毫秒 到 4 秒。
        //latency：连接延迟
        public void onConnectionUpdated(BluetoothDevice device, int interval, int latency, int timeout,
                                        int status) {
            LogUtils.e(TAG,"onConnectionUpdated  interval ");
        }


    };

    @Override
    public void sendMsgToGattClient(@NonNull byte[] respMsg, @NonNull ClientReqCallback clientReqCallback) {
        this.clientReqCallback = clientReqCallback;
        sendMsgToGattClient(respMsg);
    }

    @Override
    public void sendMsgToGattClient(@NonNull byte[] respMsg) {
        if (hasConnGattClient()) {
            clientMsgCount = 0;
            clientMsgIndex = 0;
            this.clientPackage = BleBoothHelp.Companion.splitMsgPackage(respMsg, maxMtu);
            LogUtils.e(TAG, " sendMsgToGattClient data :\n" +
                    HexStrUtils.Companion.byteArrayToHexString(respMsg) + " \n" +
                    clientPackage.length);

            if (txCharacteristic == null) {
                txCharacteristic = new BluetoothGattCharacteristic(
                        UUID.fromString(txCharUuid),
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);
            }
            txCharacteristic.setValue(clientPackage[index]);

            boolean notifyCharacteristicChanged = mBluetoothGattServer.notifyCharacteristicChanged(clientDevice, txCharacteristic, false);
            LogUtils.e(TAG, "sendMsgToGattClient \n " +
                    "---> notifyCharacteristicChanged  : " + notifyCharacteristicChanged);
        }
    }

    public boolean hasConnGattClient() {
        return hasConnClient;
    }

    public String getClientAddress() {
        return clientDevice == null ? "" : clientDevice.getAddress();
    }

    public void disConnGattClient(BluetoothDevice device) {
        mBluetoothGattServer.cancelConnection(device);
    }

    public void closeGattServer() {
        if (mBluetoothGattServer != null) {
            LogUtils.e(TAG, "closeGattServer");
            if (hasConnGattClient()) {
                disConnGattClient(clientDevice);
            }
            hasConnClient = false;
            mBluetoothGattServer.clearServices();
            mBluetoothGattServer.close();
            clientDevice = null;

            mBluetoothGattServer = null;
        }

        if (this.bleGattServerCallback != null) {
            this.bleGattServerCallback = null;
        }
    }

}
