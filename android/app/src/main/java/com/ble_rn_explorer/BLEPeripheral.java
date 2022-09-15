package com.ble_rn_explorer;

import android.bluetooth.BluetoothGattDescriptor;
import java.util.UUID;

public class BLEPeripheral {
  public static final UUID CHARACTERISTIC_USER_DESCRIPTION_UUID = UUID
      .fromString("00002901-0000-1000-8000-00805f9b34fb");
  public static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID
      .fromString("00002902-0000-1000-8000-00805f9b34fb");

  public static BluetoothGattDescriptor getClientCharacteristicConfigurationDescriptor() {
    BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
        CLIENT_CHARACTERISTIC_CONFIGURATION_UUID,
        (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
    descriptor.setValue(new byte[] { 0, 0 });
    return descriptor;
  }

  public static BluetoothGattDescriptor getCharacteristicUserDescriptionDescriptor(String defaultValue) {
    BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
        CHARACTERISTIC_USER_DESCRIPTION_UUID,
        (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
    try {
      descriptor.setValue(defaultValue.getBytes("UTF-8"));
    } finally {
      return descriptor;
    }
  }

}
