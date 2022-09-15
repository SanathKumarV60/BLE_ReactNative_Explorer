package com.ble_rn_explorer;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import com.ble_rn_explorer.BLEPeripheral;
import java.util.UUID;

public class BLECharacteristic {
    String uuid;
    int property;
    int permission;
    BluetoothGattCharacteristic gattCharacteristic;

    public BLECharacteristic(String uuid, int prop, int perm, String description) {
        this.uuid = uuid;
        this.property = prop;
        this.permission = perm;
        createCharacteristic(description);
    }

    public String getUUID() {
        return uuid;
    }

    public BluetoothGattCharacteristic getGattCharacteristic() {
        return gattCharacteristic;
    }

    void createCharacteristic(String description) {
        gattCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(uuid),
                BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                // property,
                // permission
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE);
        // BluetoothGattCharacteristic.PROPERTY_READ |
        // BluetoothGattCharacteristic.PROPERTY_NOTIFY,
        // BluetoothGattCharacteristic.PERMISSION_READ);

        /*
         * gattCharacteristic.addDescriptor(
         * BLEPeripheral.getClientCharacteristicConfigurationDescriptor());
         */
        gattCharacteristic.addDescriptor(
                BLEPeripheral.getCharacteristicUserDescriptionDescriptor(description));

    }

}
