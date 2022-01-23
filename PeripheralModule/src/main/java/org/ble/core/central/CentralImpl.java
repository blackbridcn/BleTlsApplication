package org.ble.core.central;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.se.omapi.SEService;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.ble.callback.BleGattCallback;

import org.ble.config.BleConfig;
import org.ble.exception.BleErrorCode;
import org.ble.exception.EnumErrorCode;
import org.ble.utils.BleBoothHelp;
import org.ble.utils.ObjectHelp;
import org.e.ble.utils.HexStrUtils;
import org.utils.LogUtils;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Author: yuzzha
 * Date: 2021-10-14 13:48
 * Description:
 * Remark:
 */
public class CentralImpl implements CentralCore {

    private static String TAG = CentralImpl.class.getSimpleName();

    //
    private Context mContext;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private BluetoothDevice clientDevice;

    BleConfig config;


    private int maxMtu = 20;

    private int clientMsgCount = 0;
    private int clientMsgIndex = 0;
    private byte[][] sendPackage;

    private int count;
    int index;

    public CentralImpl(Context mContext, BluetoothManager bluetoothManager, BleConfig config) {
        this.mContext = mContext.getApplicationContext();
        this.bluetoothManager = bluetoothManager;
        this.config = config;
        this.bluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public void connectGattServer(@Nullable String address, @Nullable BleGattCallback bleGattCallback) {
        executeConnectGattTask(bluetoothAdapter.getRemoteDevice(ObjectHelp.checkNotEmpty(address)), false, bleGattCallback);
    }

    @Override
    public void connectGattServer(@Nullable BluetoothDevice device, boolean autoConnect, @Nullable BleGattCallback bleGattCallback) {
        executeConnectGattTask(device, autoConnect, bleGattCallback);
    }


    @Override
    public void sendMsgToGattServerDevice(@NonNull BluetoothGatt gatt,
                                          @NonNull BluetoothGattCharacteristic characteristic,
                                          @Nullable byte[] msg) {

        LogUtils.e(TAG, "-----------------> :" + HexStrUtils.Companion.byteArrayToHexString(msg));

        count = 0;
        index = 0;
        this.sendPackage = BleBoothHelp.Companion.splitMsgPackage(msg, maxMtu);

        BluetoothGattCharacteristic txCharacteristic;

        if (TextUtils.equals(config.getTxCharacterUuid(), characteristic.getUuid().toString())) {
            txCharacteristic = characteristic;
        } else {
            BluetoothGattService service = gatt.getService(UUID.fromString(config.getServiceUuid()));
            if (service == null) {
                bleGattCallback.onConnFailure(BleErrorCode.CONN_NO_FOUND_DEVICE_UUID_SERVICE_ERROR, EnumErrorCode.getDesc(BleErrorCode.CONN_NO_FOUND_DEVICE_UUID_SERVICE_ERROR));
                onDisAndClear(gatt, this.characteristic);
                return;
            }
            LogUtils.e(TAG, "-------------------> gatt.getService  ");
            txCharacteristic = service.getCharacteristic(UUID.fromString(config.getTxCharacterUuid()));
        }
        txCharacteristic.setValue(sendPackage[index]);
        ++index;
        boolean writeCharacteristic = gatt.writeCharacteristic(characteristic);
        LogUtils.e(TAG, "-------------------->  writeCharacteristic :" + writeCharacteristic);
    }

    private void executeConnectGattTask(@Nullable BluetoothDevice device, boolean autoConnect, @Nullable BleGattCallback bleGattCallback) {
        if (!this.bluetoothAdapter.isEnabled()) {
            return;
        }
        this.bleGattCallback = bleGattCallback;
        BluetoothGatt gatt = connectGatt(device, autoConnect);
        if (gatt == null) {

        }
        this.gatt = gatt;
    }

    private BluetoothGatt gatt;

    private BleGattCallback bleGattCallback;

    private BluetoothGatt connectGatt(@Nullable BluetoothDevice device, boolean autoConnect) {
        BluetoothGatt gatt;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gatt = device.connectGatt(this.mContext, autoConnect, mGattCallback,
                    BluetoothDevice.TRANSPORT_LE,
                    BluetoothDevice.PHY_LE_1M_MASK | BluetoothDevice.PHY_LE_2M_MASK);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gatt = device.connectGatt(this.mContext, autoConnect, mGattCallback,
                    BluetoothDevice.TRANSPORT_LE);
        } else {
            gatt = device.connectGatt(this.mContext, autoConnect, mGattCallback);
        }
        return gatt;
    }

    private final Object mLock = new Object();

    private BluetoothGattCharacteristic characteristic;


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            LogUtils.e(TAG, "--------------------> onConnectionStateChange status :" + status);
          /*  if (status != BluetoothGatt.GATT_SUCCESS) {
                // BluetoothGatt.GATT_SUCCESS  表示 connect or disconnect operation 操作成功
                onDisAndClear(gatt, characteristic);
                if (bleGattCallback != null) {
                    bleGattCallback.disConnDevice();
                }
                return;
            }*/
            if (newState == BluetoothProfile.STATE_CONNECTED) {

            }
            // Check whether an error occurred
            /* if (status == BluetoothGatt.GATT_SUCCESS) {*/
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                LogUtils.e(TAG, "--------------------> onConnectionStateChange STATE_CONNECTED ");
                /*if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {*/
                LogUtils.e(TAG, "--------------------> onConnectionStateChange onConningServer ");
                bleGattCallback.onConningServer();
                // After 1.6s the services are already discovered so the following gatt.discoverServices() finishes almost immediately.
                // NOTE: This also works with shorted waiting time.
                // The gatt.discoverServices() must be called after the indication is received which is
                // about 600ms after establishing connection. Values 600 - 1600ms should be OK.

                //threadWait(600);
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 触发开始搜索服务，触发 onServicesDiscovered
                // 设备连接上 开始扫描服务
                // 开始扫描服务，安卓蓝牙开发重要步骤之一
                boolean hasServices = gatt.discoverServices();
                LogUtils.e(TAG, "--------------------> onConnectionStateChange discoverServices :" + hasServices);
                if (!hasServices) {
                    if (bleGattCallback != null) {
                        bleGattCallback.onConningFail(BleGattCallback.CONN_FAIL_DISCOVER_SERVICE);
                    }
                    onDisAndClear(gatt, characteristic);
                }
                boolean mtu = requestMtu(gatt);
                LogUtils.e(TAG, "--------------------> onConnectionStateChange mtu :" + mtu);
                /*}*/
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                if (bleGattCallback != null) {
                    bleGattCallback.disConnDevice();
                }
                onDisAndClear(gatt, characteristic);
            }
          /*  } else {
                if (status == 0x08 *//* GATT CONN TIMEOUT *//* || status == 0x13 *//* GATT CONN TERMINATE PEER USER *//*) {

                } else if (status == 0x85) { // 133

                }
            }*/
            // Notify waiting thread
        }


        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            LogUtils.e(TAG, "--------------------> onServicesDiscovered  status:" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                processServiceDiscovered(gatt, status,
                        config.getServiceUuid(),
                        config.getTxCharacterUuid(),
                        config.getRxCharacterUuid(),
                        bleGattCallback);
            }
        }

        @Override
        public void onServiceChanged(@NonNull BluetoothGatt gatt) {
            super.onServiceChanged(gatt);
            LogUtils.e(TAG, "--------------------> onServiceChanged  ");
        }

        // Other methods just pass the parameters through
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LogUtils.e(TAG, "--------------------> 数据成功发送 onCharacteristicWrite status:" + status);
            //requestMtu(gatt);
            //数据成功发送
            processCharacteristicWrite(gatt, characteristic, status, bleGattCallback);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LogUtils.e(TAG, "--------------------> onCharacteristicRead ");

        }

        //Callback triggered as a result of a remote characteristic notification.
        // GATT SERVER对characteristic进行操作后，GATT回调的通知函数 ； 就是从机发送数据了
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            LogUtils.e(TAG, "--------------------> onCharacteristicChanged  characteristic:" + characteristic.getUuid().toString());
            processCharacteristicChanged(gatt, characteristic);
        }


        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            LogUtils.e(TAG, "--------------------> onReliableWriteCompleted  characteristic:" + characteristic.getUuid().toString());

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

        }

        @SuppressLint("NewApi")
        @Override
        public void onMtuChanged(final BluetoothGatt gatt, final int mtu, final int status) {
            LogUtils.e(TAG, "--------------------> onMtuChanged mtu:" + mtu + " status :" + status);
            if (status == BluetoothGatt.GATT_SUCCESS && mtu > 23) {
                maxMtu = mtu - 3;
            }
        }

        @SuppressLint("NewApi")
        @Override
        public void onPhyUpdate(final BluetoothGatt gatt, final int txPhy, final int rxPhy, final int status) {
            LogUtils.e(TAG, "------------------------> onPhyUpdate txPhy: " + txPhy + " rxPhy:" + rxPhy);
        }


    };


    /**
     * 连接成功并且处理发现服务 ,通知用户，可以开始发送数据
     *
     * @param gatt            BluetoothGatt
     * @param status          int
     * @param serviceUUID     String
     * @param txCharacterUuid String 用来发送数据的 BluetoothGattCharacteristic的 UUID
     * @param rxCharacterUuid String  用来接受GATT_SERVER 发送来的数据  BluetoothGattCharacteristic的 的UUID
     * @param bleGattCallback BleConnCallback
     */
    public void processServiceDiscovered(BluetoothGatt gatt, int status, String
            serviceUUID, String txCharacterUuid, String rxCharacterUuid, BleGattCallback bleGattCallback) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            bleGattCallback.onConnFailure(BleErrorCode.CONN_SERVICE_STATE_ERROR, EnumErrorCode.getDesc(BleErrorCode.CONN_SERVICE_STATE_ERROR));
            onDisAndClear(gatt, this.characteristic);
            return;
        }
        BluetoothGattService service = gatt.getService(UUID.fromString(serviceUUID));
        LogUtils.e(TAG, "--------------------> processServiceDiscovered  (service == null):" + (service == null));
        if (service == null) {
            bleGattCallback.onConnFailure(BleErrorCode.CONN_NO_FOUND_DEVICE_UUID_SERVICE_ERROR, EnumErrorCode.getDesc(BleErrorCode.CONN_NO_FOUND_DEVICE_UUID_SERVICE_ERROR));
            onDisAndClear(gatt, this.characteristic);
            return;
        }
        BluetoothGattCharacteristic txCharacteristic = service.getCharacteristic(UUID.fromString(txCharacterUuid));
        LogUtils.e(TAG, "--------------------> processServiceDiscovered  (txCharacteristic == null) :" + (txCharacteristic == null));
        if (txCharacteristic == null) {
            bleGattCallback.onConnFailure(BleErrorCode.CONN_DEVICE_CHARACTERISTIC_ERROR, EnumErrorCode.getDesc(BleErrorCode.CONN_DEVICE_CHARACTERISTIC_ERROR));
            onDisAndClear(gatt, null);
            return;
        }
        //设置通知,实时监听Characteristic变化 这个很重要,如果没有设置Notification就收不到服务端/从机/外围设备返回的数据回调
        gatt.setCharacteristicNotification(txCharacteristic, true);

        BluetoothGattCharacteristic rxCharacteristic = service.getCharacteristic(UUID.fromString(rxCharacterUuid));
        LogUtils.e(TAG, "--------------------> processServiceDiscovered  (rxCharacterUuid == null) :" + (rxCharacterUuid == null));
        if (rxCharacteristic == null) {
            //bleGattCallback.onConnFailure(BleErrorCode.CONN_DEVICE_CHARACTERISTIC_ERROR, EnumErrorCode.getDesc(BleErrorCode.CONN_DEVICE_CHARACTERISTIC_ERROR));
            // onDisAndClear(gatt, null);
            // return;
        } else {
            boolean notification = gatt.setCharacteristicNotification(rxCharacteristic, true);
            LogUtils.e(TAG, "--------------------> processServiceDiscovered   :" + notification);
        }

        bleGattCallback.onConnSuccess(gatt, txCharacteristic);
        // CONN_STATE = BleState.ConnState.CONN_STATE_CONNECTED;
        bleGattCallback.startSendMsg(gatt, txCharacteristic);
    }

    //MTU（Maximum Transmission Unit），最大传输单元
    private boolean requestMtu(BluetoothGatt gatt) {
        if (gatt != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 25 *6 +3 =153
            return gatt.requestMtu(515);
        }
        return false;
    }

    //低延时 、提高数据传递速率
    private boolean requestHighPriority(BluetoothGatt gatt) {
        if (gatt != null) {
            return requestConnectionPriority(gatt, BluetoothGatt.CONNECTION_PRIORITY_HIGH);
        }
        return false;
    }

    //系统默认为CONNECTION_PRIORITY_BALANCED
    private boolean requestConnectionPriority(BluetoothGatt gatt, int connectionPriority) {
        if (connectionPriority < BluetoothGatt.CONNECTION_PRIORITY_BALANCED
                || connectionPriority > BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER) {
            return gatt.requestConnectionPriority(connectionPriority);
        }
        return false;
    }

    /**
     * 数据发送成功后的,Ble回调函数
     * <p>
     * 检测数据包是否全部发送完成，如果没有继续发送，否则置空；
     */
    public void processCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
            characteristic, int status, BleGattCallback bleGattCallback) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            bleGattCallback.onConnFailure(BleErrorCode.CONN_WRITE_SERVICE_STATE_ERROR, EnumErrorCode.getDesc(BleErrorCode.CONN_WRITE_SERVICE_STATE_ERROR));
            onDisAndClear(gatt, characteristic);
            return;
        }
        LogUtils.e(TAG, "-------------------------> count  :"+count);
        count++;
        if (count < this.sendPackage.length) {
            //  String hexString = ByteHexUtils.INSTANCE.byteArrayToHexString(msgPackage[count]);
            BluetoothGattService service = gatt.getService(UUID.fromString(config.getServiceUuid()));
            if (service != null) {

            }
            characteristic.setValue(this.sendPackage[count]);
            gatt.writeCharacteristic(characteristic);
        } else {
            this.sendPackage = null;
        }

    }

    /**
     * 处理返回消息数据
     *
     * @param gatt
     * @param characteristic
     */
    public void processCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
            characteristic) {
        LogUtils.e(TAG, "处理返回消息数据 " + characteristic.getUuid().toString());
        if (this.bleGattCallback != null) {
            BluetoothGattService gattService = gatt.getService(UUID.fromString(config.getServiceUuid()));
            if (gattService != null) {
                LogUtils.e(TAG,"-----------------------------> 处理返回消息数据  gatt.getService");
                this.bleGattCallback.onBleServerResp(gatt, gattService.getCharacteristic(UUID.fromString(config.getTxCharacterUuid())), characteristic.getValue(), maxMtu);
            } else {
                this.bleGattCallback.onBleServerResp(gatt, characteristic, characteristic.getValue(), maxMtu);
            }
        }
    }

    private void terminate() {

    }

    private void threadWait(final long millis) {
        synchronized (mLock) {
            try {
                LogUtils.i(TAG, "thread blocking " + millis + " milliseconds ");
                mLock.wait(millis);
            } catch (final InterruptedException e) {
                LogUtils.e(TAG, "Sleeping interrupted " + e.getMessage());
            }
        }
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
        //CONN_STATE = BleState.ConnState.CONN_STATE_IDLE;
        //clearCallback();
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
}
