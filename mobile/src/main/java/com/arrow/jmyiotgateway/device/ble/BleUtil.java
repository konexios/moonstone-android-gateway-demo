package com.arrow.jmyiotgateway.device.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BleUtil {
    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null)
            return null;
        return bluetoothManager.getAdapter();
    }

    public static BleStatus getBleStatus(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return BleStatus.BLE_NOT_AVAILABLE;
        }

        BluetoothAdapter adapter = getBluetoothAdapter(context);
        if (adapter == null) {
            return BleStatus.BLUETOOTH_NOT_AVAILABLE;
        }

        if (!adapter.isEnabled()) {
            return BleStatus.BLUETOOTH_DISABLED;
        }

        return BleStatus.BLE_AVAILABLE;
    }

    public static Integer shortSignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset + 1]; // // Interpret MSB as signed
        return (upperByte << 8) + lowerByte;
    }

    public static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset + 1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }

    public static Integer twentyFourBitUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer mediumByte = (int) c[offset + 1] & 0xFF;
        Integer upperByte = (int) c[offset + 2] & 0xFF;
        return (upperByte << 16) + (mediumByte << 8) + lowerByte;
    }

    public static short bytesToInt16(byte[] arr, int start) {
        return ByteBuffer.wrap(arr, start, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static int bytesToInt32(byte[] arr, int start) {
        return ByteBuffer.wrap(arr, start, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static short byteToUInt8(byte[] arr ,int index){
        return (short)(arr[index] &  0xFF);
    }
}
