package org.ble.core;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;

import android.content.Context;
import android.content.Intent;
import android.os.Build;


import androidx.annotation.NonNull;

import org.ble.callback.BleGattCallback;
import org.ble.callback.BleGattServerCallback;
import org.ble.callback.BleMsgCallback;
import org.ble.core.central.CentralCore;
import org.ble.core.central.CentralImpl;
import org.ble.core.peripheral.PeripheralCore;
import org.ble.core.peripheral.PeripheralImpl;

import org.ble.callback.ClientReqCallback;
import org.ble.config.BleConfig;
import org.ble.exception.BleErrorCode;
import org.ble.exception.EnumErrorCode;
import org.ble.scan.BluetoothScannerProvider;
import org.ble.scan.BlzScanCallback;
import org.ble.utils.BleBoothHelp;
import org.ble.utils.ObjectHelp;

import org.utils.LogUtils;
import org.utils.StringUtils;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Author: yuzzha
 * Date: 2021-04-09 11:22
 * Description:
 * Remark:
 */
public class BleCoreImpl implements BleCore, BluetoothAdapter.LeScanCallback {

    private static String TAG = BleCoreImpl.class.getSimpleName();

    private Context mContext;
    private BleConfig config;

    BluetoothManager bluetoothManager;
    BluetoothAdapter mBluetoothAdapter;

    private CentralCore central;
    private PeripheralCore peripheral;


    private BlzScanCallback scanCallback;
    private BleGattCallback bleGattCallback;

    //????????????????????????????????????????????????????????????  onCharacteristicChanged ????????????????????????
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;

    /*----------------------------------------------------------------------------------*/
    //????????????--->??????????????????????????????
    private int CONN_STATE = BleState.ConnState.CONN_STATE_IDLE;
    //??????????????????
    private int SCAN_STATE = BleState.ScanState.SCAN_STATE_IDLE;
    /*----------------------------------------------------------------------------------*/

    private byte[][] msgPackage;
    private int count;
    int index;
    private BleMsgCallback msgCallback;

    private BleCoreImpl() {
    }

    private final static class BleManagerHolder {
        private static final BleCoreImpl Instance = new BleCoreImpl();
    }

    public static BleCoreImpl getInstance() {
        return BleManagerHolder.Instance;
    }

    public void init(Context mContext, BleConfig config) {
        assert mContext != null;
        this.mContext = mContext.getApplicationContext();
        this.config = config;
        this.bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = bluetoothManager.getAdapter();

        central = new CentralImpl(this.mContext,bluetoothManager,config);
        if (config.isPeripheral()) {
            peripheral = new PeripheralImpl(bluetoothManager, this.mContext);
        }

        if (StringUtils.isNotEmpty(config.getBleName())) {
            boolean blzName = this.mBluetoothAdapter.setName(config.getBleName());
            if (!blzName) {
                LogUtils.i(TAG, " Bluetooth set name fail ,\n " +
                        " turn off Bluetooth Or \n " +
                        " valid Bluetooth names are a maximum of 248 bytes using UTF-8 encoding, \n" +
                        " although many remote devices can only display the first 40 characters, \n" +
                        " and some may be limited to just 20.");
            }
        }
    }

    @Override
    public void startScan() {
        this.startScan(new UUID[]{UUID.fromString(config.getServiceUuid())});
    }

    @Override
    public void startScan(UUID[] serviceUuids) {
        startScan(serviceUuids, this.config.getScanTimeout(), false);
    }

    @Override
    public void startScan(long scanTimeout) {
        this.startScan(new UUID[]{UUID.fromString(config.getServiceUuid())}, scanTimeout, true);
    }

    public void startScan(UUID[] serviceUuids, long scanTimeout, boolean bootstrap) {
        if (bootstrap && SCAN_STATE == BleState.ScanState.SCAN_STATE_DOING) {
            stopScan();
        }
        if (SCAN_STATE == BleState.ScanState.SCAN_STATE_IDLE) {
            SCAN_STATE = BleState.ScanState.SCAN_STATE_DOING;
            if (this.scanCallback != null) {
                this.scanCallback.onPreScan();
            }
            //Android 7.0 ????????????30????????????+????????????5??? ,
            //Android 10 ??????BLE?????????????????????GPS???
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //List<ScanFilter> filters, ScanSettings settings,
               /* if (config.isNewApi()) {
                    // Scanning settings
                    ScanSettings settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                            //.setLegacy(false)
                            .setReportDelay(500)
                            // .setUseHardwareBatchingIfSupported(false)
                            .build();

                } else {*/
                    this.mBluetoothAdapter.startLeScan(this);
               // }
            } else {
                if (serviceUuids != null) {
                    // this.mBluetoothAdapter.startLeScan(serviceUuids, this);
                    this.mBluetoothAdapter.startLeScan(this);
                } else {
                    this.mBluetoothAdapter.startLeScan(this);
                }
            }
            if (scanTimeout > 0) {
                delayStopScanTask(scanTimeout);
            }

        } else {
            LogUtils.e(TAG, "------------------> ble scaning ?????? ");
        }
    }

    @Override
    public void setScanCallback(BlzScanCallback callbacks) {
        if (callbacks != null) {
            long scanTimeout = callbacks.getScanTimeou();
            if (scanTimeout != -1) {
                this.config.setScanTimeout(scanTimeout);
            }
        }
        this.scanCallback = callbacks;
    }


    @Override
    public void stopScan() {
        if (SCAN_STATE == BleState.ScanState.SCAN_STATE_DOING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                /*if (config.isNewApi()) {
                    BluetoothAdapter.getDefaultAdapter().
                            getBluetoothLeScanner().stopScan(leScanCallback);
                    //this.leScanner.stopScan(leScanCallback);
                } else {*/
                    this.mBluetoothAdapter.stopLeScan(this);
                //}
            } else {
                this.mBluetoothAdapter.stopLeScan(this);
            }
            SCAN_STATE = BleState.ScanState.SCAN_STATE_IDLE;
            if (this.scanCallback != null) {
                this.scanCallback.onScanFinsh();
            }
        }
    }

    @Override
    public boolean isConnect() {
        return (CONN_STATE & (BleState.ConnState.CONN_STATE_CONNECTING | BleState.ConnState.CONN_STATE_CONNECTED)) != 0;
    }

    private void resetParams() {
        this.bleGattCallback = null;
        msgPackage = null;
        count = 0;
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        if (characteristic != null) {
            characteristic = null;
        }
    }

    @Override
    public boolean isEnabled() {
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.isEnabled();
        }
        return false;
    }

    public boolean enableBluetooth(Activity mActivity, int requestCode) {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBtIntent, requestCode);
        }
        return false;
    }


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (this.scanCallback != null) {
            this.scanCallback.onLeScan(device, rssi, scanRecord);
        }
    }

    @Override
    public boolean isScan() {
        return SCAN_STATE == BleState.ScanState.SCAN_STATE_DOING;
    }

    public void connectGattServer(String address, BleGattCallback bleGattCallback) {
        central.connectGattServer(address,bleGattCallback);
    }

    @Override
    public void connectGattServer(BluetoothDevice device, boolean autoConnect, BleGattCallback
            bleGattCallback) {
        central.connectGattServer(device,autoConnect,bleGattCallback);
    }

    @Override
    public void sendMsgToGattServerDevice(BluetoothGatt bluetoothGatt,
                                          BluetoothGattCharacteristic characteristic,
                                          byte[] msg) {
        central.sendMsgToGattServerDevice(bluetoothGatt,
                characteristic,
                msg);
    }


    /**
     * ?????????????????? ??????--> ????????????
     *
     * @param scanTimeout ????????????
     *                    ??????????????????????????????????????????
     */
    private void delayStopScanTask(@NonNull long scanTimeout) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopScan();
            }
        }, scanTimeout);
    }

    public void disConnGattServer() {
        onDisAndClear(this.bluetoothGatt, this.characteristic);
    }

    public void disConnGattServer(BluetoothGatt gatt, BluetoothGattCharacteristic
            characteristic) {
        onDisAndClear(gatt, characteristic);
    }

    private void onDisAndClear(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (gatt != null) {
            gatt.disconnect();
        }
        if (gatt != null) {
            refreshDeviceCache(gatt);
        }
        if (gatt != null) {
            gatt.close();
        }
        if (gatt != null) {
            gatt = null;
        }
        if (characteristic != null) {
            characteristic = null;
        }
        CONN_STATE = BleState.ConnState.CONN_STATE_IDLE;
        clearCallback();
    }

    private void clearCallback() {
        //ble scan
        scanCallback = null;
        //ble conn
        bleGattCallback = null;
        msgCallback = null;
        msgPackage = null;
        count = 0;
    }


    /**
     * ???????????????????????????????????? ,???????????????????????????????????????
     *
     * @param gatt            BluetoothGatt
     * @param status          int
     * @param serviceUUID     String
     * @param characterUUID   String
     * @param bleGattCallback BleConnCallback
     */
    public void processServiceDiscovered(BluetoothGatt gatt, int status, String
            serviceUUID, String characterUUID, BleGattCallback bleGattCallback) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            bleGattCallback.onConnFailure(BleErrorCode.CONN_SERVICE_STATE_ERROR, EnumErrorCode.getDesc(BleErrorCode.CONN_SERVICE_STATE_ERROR));
            onDisAndClear(gatt, this.characteristic);
            return;
        }
        BluetoothGattService service = gatt.getService(UUID.fromString(serviceUUID));
        if (service == null) {
            bleGattCallback.onConnFailure(BleErrorCode.CONN_NO_FOUND_DEVICE_UUID_SERVICE_ERROR, EnumErrorCode.getDesc(BleErrorCode.CONN_NO_FOUND_DEVICE_UUID_SERVICE_ERROR));
            onDisAndClear(gatt, this.characteristic);
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characterUUID));
        if (characteristic == null) {
            bleGattCallback.onConnFailure(BleErrorCode.CONN_DEVICE_CHARACTERISTIC_ERROR, EnumErrorCode.getDesc(BleErrorCode.CONN_DEVICE_CHARACTERISTIC_ERROR));
            onDisAndClear(gatt, null);
            return;
        }
        //????????????,????????????Characteristic?????? ???????????????,??????????????????Notification?????????????????????/??????/?????????????????????????????????
        gatt.setCharacteristicNotification(characteristic, true);
        bleGattCallback.onConnSuccess(gatt, characteristic);
        CONN_STATE = BleState.ConnState.CONN_STATE_CONNECTED;
        bleGattCallback.startSendMsg(gatt, characteristic);
    }


    /**
     * ????????????????????????,Ble????????????
     * <p>
     * ????????????????????????????????????????????????????????????????????????????????????
     */
    public void processCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
            characteristic, int status, BleGattCallback bleGattCallback) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            bleGattCallback.onConnFailure(BleErrorCode.CONN_WRITE_SERVICE_STATE_ERROR, EnumErrorCode.getDesc(BleErrorCode.CONN_WRITE_SERVICE_STATE_ERROR));
            onDisAndClear(gatt, characteristic);
            return;
        }
        count++;
        if (count < this.msgPackage.length) {
            //  String hexString = ByteHexUtils.INSTANCE.byteArrayToHexString(msgPackage[count]);
            characteristic.setValue(this.msgPackage[count]);
            gatt.writeCharacteristic(characteristic);
        } else {
            this.msgPackage = null;
        }
    }

    /**
     * ????????????????????????
     *
     * @param gatt
     * @param characteristic
     */
    public void processCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
            characteristic) {
        if (this.msgCallback != null) {
            this.msgCallback.receiverRemoteBleMsg(gatt, characteristic);
        }
    }

    public boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {

        }
        return false;
    }

    /*----------------------------------------------------------------------------------*/

    public boolean supportAdvertisement() {
        if (mBluetoothAdapter == null) {
            throw new RuntimeException(" should be init initBluetooth ");
        }
        return mBluetoothAdapter.isMultipleAdvertisementSupported();
    }

    @Override
    public void startAdvertising(AdvertiseSettings settings,
                                 AdvertiseData advertiseData,
                                 final AdvertiseCallback callback) {
        if (peripheral != null) {
            peripheral.startAdvertising(settings, advertiseData, callback);
        }
    }

    @Override
    public void startAdvertising(String serviceUUID, final AdvertiseCallback callback) {
        if (peripheral != null) {
            peripheral.startAdvertising(serviceUUID, callback);
        }
    }

    @Override
    public void startAdvertising(byte[] serviceUUID, final AdvertiseCallback callback) {
        if (peripheral != null) {
            peripheral.startAdvertising(serviceUUID, callback);
        }
    }

    @Override
    public void stopAdvertising(final AdvertiseCallback callback) {
        if (peripheral != null) {
            peripheral.stopAdvertising(callback);
        }
    }

    @Override
    public void setGattServerCallback(@NonNull BleGattServerCallback serverCallback) {
        if (peripheral != null) {
            peripheral.setGattServerCallback(serverCallback);
        }
    }

    public void startGattServer(String serviceUuid, String rxCharUuid, String
            txCharUuid, String descUuid, BleGattServerCallback serverCallback) {
        setGattServerCallback(serverCallback);
        startGattServer(serviceUuid, rxCharUuid, txCharUuid, descUuid);
    }

    public void sendMsgToGattClient(byte[] respMsg, ClientReqCallback clientReqCallback) {
        if (peripheral != null) {
            peripheral.sendMsgToGattClient(respMsg, clientReqCallback);
        }
    }

    public void sendMsgToGattClient(byte[] respMsg) {
        if (peripheral != null) {
            peripheral.sendMsgToGattClient(respMsg);
        }
    }


    public void startGattServer(String serviceUuid, String rxCharUuid, String
            txCharUuid, String mCCCDUuid) {
        if (peripheral != null) {
            peripheral.startGattServer(serviceUuid, rxCharUuid, txCharUuid, mCCCDUuid);
        }
    }


    public boolean hasConnGattClient() {
        if (peripheral != null) {
            return peripheral.hasConnGattClient();
        }
        return false;
    }

    public String getClientAddress() {
        if (peripheral != null) {
            return peripheral.getClientAddress();
        }
        return "";
    }

    public void disConnGattClient(BluetoothDevice device) {
        if (peripheral != null) {
            peripheral.disConnGattClient(device);
        }
    }

    public void closeGattServer() {
        if (peripheral != null) {
            peripheral.closeGattServer();
        }
    }

}
