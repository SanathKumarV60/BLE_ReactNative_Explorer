package com.ble_rn_explorer;

import java.util.ArrayList;
import java.util.List;

import java.util.UUID;
import java.util.Arrays;
import java.util.HashSet;

import android.util.Log;

import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.ble_rn_explorer.BLECharacteristic;
import com.ble_rn_explorer.BLEPeripheral;

import com.facebook.react.bridge.Callback;

public class BLEService {

    private String name;
    private String uuid;
    private List<BLECharacteristic> characteristics;
    private BluetoothGattService blGattService;
    private BluetoothGattServer gattServer;
    private BluetoothManager btManager;
    private Context context;

    private Callback charWriteCallback;
    private Callback gattCallback;

    private HashSet<BluetoothDevice> btDevices;

    public static final String LOG_TAG = "BLEService";

    public List<BluetoothDevice> getDevices() {
        ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
        list.addAll(btDevices);
        return list;
    }

    public void setWriteCallback(Callback cb) {
        charWriteCallback = cb;
    }

    public void setGattServerCallback(Callback cb){
        gattCallback = cb;
    }
    public void setContext(Context ctx) {
        context = ctx;
    }

    public void setBluetoothManager(BluetoothManager btManager) {
        this.btManager = btManager;
    }

    public BLEService(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
        characteristics = new ArrayList<BLECharacteristic>();
        btDevices = new HashSet<BluetoothDevice>();
    }

    public void addCharacteristic(BLECharacteristic ch) {
        characteristics.add(ch);
        Log.i(LOG_TAG, "addCharacterstics: " + ch.getUUID() + ": #chars=" + characteristics.size());
    }

    public List<BLECharacteristic> getCharacteristics() {
        return characteristics;
    }

    public String getUUID() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public BluetoothGattService getGattService() {
        return blGattService;
    }

    
    public void setupService() {

        Log.i(LOG_TAG, "setupService: " + name + ": #chars=" + characteristics.size());

        blGattService = new BluetoothGattService(
                UUID.fromString(uuid),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        for (BLECharacteristic c : characteristics) {
            Log.i(LOG_TAG, "add char to service: " + name + ":" + c.getUUID());

            blGattService.addCharacteristic(c.getGattCharacteristic());
        }
        gattServer = btManager.openGattServer(context, gattServerCallback);
        gattServer.addService(blGattService);
    }

    public void sendResponse(BluetoothDevice device, int requestId, int status, int offset) {

        gattServer.sendResponse(device, requestId, status, offset, null);

    }

    private final BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
            // super.onConnectionStateChange(device, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    btDevices.add(device);
                    // updateConnectedDevicesStatus();
                    Log.i(LOG_TAG, "Connected to device: " + device.getAddress());
                    BLEPeripheralModule.sendNotification("DEVICE_CONNECTED",
                        new String[]{"deviceAddress"},
                        new String[]{device.getAddress()}
                    );
                   
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    btDevices.remove(device);
                    // updateConnectedDevicesStatus();
                    Log.i(LOG_TAG, "Disconnected from device");
                    BLEPeripheralModule.sendNotification("DEVICE_DISCONNECTED",
                        new String[]{"deviceAddress"},
                        new String[]{device.getAddress()}
                     );
               
                  
                }
            } else {
                btDevices.remove(device);
                Log.e(LOG_TAG, "Error when connecting");
           
                BLEPeripheralModule.sendNotification("DEVICE_CONNECT_ERROR",
                    new String[]{"deviceAddress"},
                    new String[]{device.getAddress()}
                );
       
                
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                int offset, byte[] value) {

            Log.i(LOG_TAG, "Characteristic Write request: " + Arrays.toString(value));
            if (charWriteCallback != null)
                charWriteCallback.invoke(device.getAlias(), requestId, characteristic.getUuid(), offset, value,
                        responseNeeded);
            else if (responseNeeded)
                sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset);

        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
                int offset, BluetoothGattDescriptor descriptor) {

            Log.d(LOG_TAG, "Device tried to read descriptor: " + descriptor.getUuid());
            Log.d(LOG_TAG, "Value: " + Arrays.toString(descriptor.getValue()));

            BLEPeripheralModule.sendNotification("DESCR_READ_REQUEST",
                new String[]{"deviceAddress","requestId","offset","descriptorUUID"},
                new String[]{device.getAddress(),String.valueOf(requestId), String.valueOf(offset),descriptor.getUuid().toString()}
            );
            
            if (offset != 0) {
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
                        /* value (optional) */ null);
                return;
            }
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    descriptor.getValue());
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
                int offset,
                byte[] value) {

            Log.i(LOG_TAG, "Descriptor Write Request " + descriptor.getUuid() + " " + Arrays.toString(value));

            BLEPeripheralModule.sendNotification("DESCR_WRITE_REQUEST",
                new String[]{"deviceAddress","requestId","offset","descriptorUUID"},
                new String[]{device.getAddress(),String.valueOf(requestId), String.valueOf(offset),descriptor.getUuid().toString()}
            );
           
            int status = BluetoothGatt.GATT_SUCCESS;
            if (descriptor.getUuid() == BLEPeripheral.CLIENT_CHARACTERISTIC_CONFIGURATION_UUID) {
                BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();

                boolean supportsNotifications = (characteristic.getProperties() &
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
                boolean supportsIndications = (characteristic.getProperties() &
                        BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0;

                if (!(supportsNotifications || supportsIndications)) {
                    status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
                } else if (value.length != 2) {
                    status = BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
                } else if (Arrays.equals(value, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
                    status = BluetoothGatt.GATT_SUCCESS;

                    descriptor.setValue(value);
                } else if (supportsNotifications &&
                        Arrays.equals(value, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                    status = BluetoothGatt.GATT_SUCCESS;

                    descriptor.setValue(value);
                } else if (supportsIndications &&
                        Arrays.equals(value, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
                    status = BluetoothGatt.GATT_SUCCESS;

                    descriptor.setValue(value);
                } else {
                    status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
                }
            } else {
                status = BluetoothGatt.GATT_SUCCESS;
                descriptor.setValue(value);
            }
            if (responseNeeded) {
                gattServer.sendResponse(device, requestId, status,
                        /* No need to respond with offset */ 0,
                        /* No need to respond with a value */ null);
            }
        }
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
         BluetoothGattCharacteristic characteristic){
            
            BLEPeripheralModule.sendNotification("CHARC_READ_REQUEST",
                new String[]{"deviceAddress","requestId","offset","CharacteristicUUID"},
                new String[]{device.getAddress(),String.valueOf(requestId), String.valueOf(offset),characteristic.getUuid().toString()}
            );
       
        }
        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu){
          
            BLEPeripheralModule.sendNotification("DEVICE_MTU_CHANGED",
                new String[]{"deviceAddress","mtu"},
                new String[]{device.getAddress(), String.valueOf(mtu)}
            );
       
        }
        @Override
        public void 
        onServiceAdded(int status, BluetoothGattService service){
            BLEPeripheralModule.sendNotification("SERVICE_ADDED",
                new String[]{"status","serviceUuid"},
                new String[]{ String.valueOf(status),service.getUuid().toString()}
            );
       
        }
        @Override
        public void 
        onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status){
          
            BLEPeripheralModule.sendNotification("PHY_UPDATED",
                new String[]{"deviceAddress","status"},
                new String[]{ device.getAddress(),String.valueOf(status)}
            );
            
        }
        @Override
        public void 
        onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {
            
            BLEPeripheralModule.sendNotification("PHY_READ",
                new String[]{"deviceAddress","status"},
                new String[]{ device.getAddress(),String.valueOf(status)}
            );
            
        }
    };
}
