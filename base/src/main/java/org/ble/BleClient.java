package org.ble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.ble.callback.BleGattCallback;
import org.ble.callback.BleGattServerCallback;
import org.ble.callback.BleMsgCallback;
import org.ble.scan.BlzScanCallback;
import org.ble.callback.ClientReqCallback;
import org.ble.config.BleConfig;
import org.ble.core.BleCore;
import org.ble.core.BleCoreImpl;
import org.ble.help.LocationReceiver;
import org.ble.help.LocationChangeListener;
import org.ble.utils.ObjectHelp;

import org.utlis.ByteHexUtils;


/**
 * Author: yuzzha
 * Date: 2021-04-09 15:12
 * Description:
 * Remark:
 */
public class BleClient {

    private static String TAG = BleClient.class.getSimpleName();

    private Context mContext;

    private BleCore mBlutoothClent;
    private static BleClient instance;

    private LocationReceiver locationReceiver;

    public static BleClient getInstance() {
        return instance;
    }

    public static void initBlutooth(Context mContext, BleConfig bleConfig) {
        if (instance == null) {
            synchronized (BleClient.class) {
                if (instance == null)
                    instance = new BleClient(mContext, bleConfig);
            }
        }
    }

    private BleClient(Context context, BleConfig bleConfig) {
        if (this.mContext != null) {
            throw new RuntimeException("U Can't repeat initBluetooth");//BLUETOOTH
        }
        Context mContexts = ObjectHelp.checkNotNull(context, "Context must not be null ");
        this.mContext = mContexts.getApplicationContext();
        if (checkBluetoothSupport(this.mContext)) {
            mBlutoothClent = BleCoreImpl.getInstance();
            mBlutoothClent.init(mContext, bleConfig);
        }
    }


    public boolean isSupportBle(Context mContext) {
        return checkBluetoothSupport(mContext);
    }

    private boolean checkBluetoothSupport(Context mContext) {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //Log.w(TAG, "Bluetooth LE is not supported");
            return false;
        }
        return true;
    }

    /**
     * BLE是否可以立即连接
     *
     * @return Ready Statue
     */
    public boolean isReady() {
        return !(mBlutoothClent.isConnect() | mBlutoothClent.isScan());
    }

    public boolean isReadyScan() {
        return !mBlutoothClent.isScan();
    }

    public void sendMsgToGattServerDevice(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, String hexStr, BleMsgCallback msgCallback) {
        if (mBlutoothClent != null && mBlutoothClent.isConnect()) {
            sendMsgToGattServerDevice(gatt, characteristic, ByteHexUtils.INSTANCE.hexToByteArray(hexStr), msgCallback);
        }
    }

    public void sendMsgToGattServerDevice(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] msg, BleMsgCallback msgCallback) {
        if (mBlutoothClent != null) {
            mBlutoothClent.sendMsgToGattServerDevice(gatt, characteristic, msg);
        }
    }

    public void startScan(BlzScanCallback mBleScanCallback) {
        if (mBlutoothClent != null) {
            mBlutoothClent.setScanCallback(mBleScanCallback);
            mBlutoothClent.startScan();
        }
    }

    public void startScan(BlzScanCallback mBleScanCallback, long scanTimeout) {
        if (mBlutoothClent != null) {
            mBlutoothClent.setScanCallback(mBleScanCallback);
            mBlutoothClent.startScan(scanTimeout);
        }
    }


    public void stopLeScan() {
        if (mBlutoothClent != null) {
            mBlutoothClent.stopScan();
        }
    }

    public void connectGattServer(String address, BleGattCallback bleGattCallback) {
        if (mBlutoothClent != null) {
            mBlutoothClent.connectGattServer(address, bleGattCallback);
        }
    }

    public void connectGattServer(BluetoothDevice device, BleGattCallback bleGattCallback) {
        if (mBlutoothClent != null) {
            mBlutoothClent.connectGattServer(device, false, bleGattCallback);
        }
    }

    public void connectGattServer(BluetoothDevice device, boolean autoConnect, BleGattCallback bleGattCallback) {
        if (mBlutoothClent != null) {
            mBlutoothClent.connectGattServer(device, autoConnect, bleGattCallback);
        }
    }

    public void startAdvertising(AdvertiseSettings settings,
                                 AdvertiseData advertiseData,
                                 final AdvertiseCallback callback) {
        if (this.mBlutoothClent != null) {
            this.mBlutoothClent.startAdvertising(settings, advertiseData, callback);
        }
    }

    public void startAdvertising(byte[] serviceUUID, final AdvertiseCallback callback) {
        if (mBlutoothClent != null) {
            this.mBlutoothClent.startAdvertising(
                    serviceUUID,
                    callback);
        }
    }


    public void startAdvertising(String serviceUUID, final AdvertiseCallback callback) {
        if (mBlutoothClent != null) {
            this.mBlutoothClent.startAdvertising(
                    serviceUUID,
                    callback);
        }
    }


    public void stopAdvertising(final AdvertiseCallback callback) {
        if (this.mBlutoothClent != null) {
            this.mBlutoothClent.stopAdvertising(callback);
        }
    }

    public void startGattServer(String serviceUuid, String rxCharUuid, String txCharUuid, String mCCCDUuid, BleGattServerCallback serverCallback) {
        setGattServerCallback(serverCallback);
        startGattServer(serviceUuid, rxCharUuid, txCharUuid, mCCCDUuid);
    }

    public void startGattServer(String serviceUuid, String rxCharUuid, String txCharUuid, String mCCCDUuid) {
        if (this.mBlutoothClent != null) {
            this.mBlutoothClent.startGattServer(serviceUuid, rxCharUuid, txCharUuid, mCCCDUuid);
        }
    }

    public void setGattServerCallback(BleGattServerCallback serverCallback) {
        if (this.mBlutoothClent != null) {
            this.mBlutoothClent.setGattServerCallback(serverCallback);
        }
    }

    public void sendMsgToGattClient(byte[] respVo) {
        this.mBlutoothClent.sendMsgToGattClient(respVo);
    }

    public void sendMsgToGattClient(byte[] respVo, ClientReqCallback reqCallback) {
        this.mBlutoothClent.sendMsgToGattClient(respVo, reqCallback);
    }

    public boolean hasGattClientConn() {
        if (this.mBlutoothClent != null) {
            return this.mBlutoothClent.hasConnGattClient();
        }
        throw new RuntimeException();
        // return false;
    }

    public String getClientAddress() {
        return this.mBlutoothClent.getClientAddress();
    }

    public void disConnGattClient(BluetoothDevice device) {
        this.mBlutoothClent.disConnGattClient(device);
    }

    public void closeGattServer() {
        this.mBlutoothClent.closeGattServer();
    }

    public boolean isScanning() {
        return mBlutoothClent.isScan();
    }

    public boolean isConnect() {
        return mBlutoothClent.isConnect();
    }

    public void disConnGattServer() {
        mBlutoothClent.disConnGattServer();
    }

    public void disConnGattServer(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        mBlutoothClent.disConnGattServer(gatt, characteristic);
    }

    public void doResetBleTask() {
        if (isScanning()) {
            stopLeScan();
        }
        if (isConnect()) {
            disConnGattServer();
        }
    }

    public boolean canBlz() {
        if (mBlutoothClent != null) {
            return mBlutoothClent.isEnabled();
        }
        return false;
    }

    public void doEnableBlz(Activity mActivity, int requestCode) {
        if (mBlutoothClent != null) {
            mBlutoothClent.enableBluetooth(mActivity, requestCode);
        }
    }

    public void registerLocationChangeListener(@NonNull Context mContext, @NonNull LocationChangeListener locationService) {
        LocationReceiver.Companion.getInstance().addLocationChangeListener(mContext,locationService);
    }

    public void unRegisterLocationChangeListener(@NonNull LocationChangeListener locationService) {
        if (locationReceiver != null) {
            LocationReceiver.Companion.getInstance().removeLocationChangeListener(locationService);
        }
    }

    public void sendMsgToGattServerDevice(
            @NonNull BluetoothGatt gatt,
            @NonNull BluetoothGattCharacteristic characteristic,
            @Nullable byte[] buildInitMsg) {

        mBlutoothClent.sendMsgToGattServerDevice(gatt,characteristic,buildInitMsg);
    }
}
