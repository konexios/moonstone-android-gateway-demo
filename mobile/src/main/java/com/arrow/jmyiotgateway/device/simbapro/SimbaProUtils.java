package com.arrow.jmyiotgateway.device.simbapro;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by osminin on 15.01.2018.
 */

public class SimbaProUtils {
    public static final int MAX_STRING_SIZE_TO_SENT = 20;
    public static final int MAC_ADDRESS_LENGTH = 6;

    public static short bytesToInt16(byte[] arr, int start) {
        return ByteBuffer.wrap(arr, start, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static int bytesToInt32(byte[] arr, int start) {
        return ByteBuffer.wrap(arr, start, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static int write(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic,
                     byte[] data, int offset, int byteToSend) {
        int byteSend = offset;
        //write the message with chunk of MAX_STRING_SIZE_TO_SENT bytes
        while ((byteToSend - byteSend) > MAX_STRING_SIZE_TO_SENT) {
            if (writeCharacteristic(gatt, characteristic, Arrays.copyOfRange(data, byteSend, byteSend + MAX_STRING_SIZE_TO_SENT))) {
                byteSend += MAX_STRING_SIZE_TO_SENT;
            }

        }//while

        //send the remaining data
        if (byteSend != byteToSend) {
            writeCharacteristic(gatt, characteristic, Arrays.copyOfRange(data, byteSend, byteToSend));
        }
        return byteToSend;
    }

    private static boolean writeCharacteristic(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic,
                                        final byte[] data) {
        if (gatt == null) {
            return false;
        }
        characteristic.setValue(data);
        return gatt.writeCharacteristic(characteristic);
    }

    public static long macAddressToLong(String macAddress) {
        byte[] address = macAddressToBytes(macAddress);
        long mac = 0;
        for (int i = 0; i < 6; i++) {
            long t = (address[i] & 0xffL) << ((5 - i) * 8);
            mac |= t;
        }
        return mac;
    }

    public static byte[] macAddressToBytes(String address) {
        String[] elements = address.split(":");
        if (elements.length != MAC_ADDRESS_LENGTH) {
            throw new IllegalArgumentException(
                    "Specified MAC Address must contain 12 hex digits" +
                            " separated pairwise by :'s.");
        }

        byte[] addressInBytes = new byte[MAC_ADDRESS_LENGTH];
        for (int i = 0; i < MAC_ADDRESS_LENGTH; i++) {
            String element = elements[i];
            addressInBytes[i] = (byte)Integer.parseInt(element, 16);
        }

        return addressInBytes;
    }

    public static String macAddressToString(long address) {
        byte[] addressInBytes = new byte[] {
                (byte)((address >> 40) & 0xff),
                (byte)((address >> 32) & 0xff),
                (byte)((address >> 24) & 0xff),
                (byte)((address >> 16) & 0xff),
                (byte)((address >> 8 ) & 0xff),
                (byte)((address >> 0) & 0xff)
        };
        StringBuilder builder = new StringBuilder();
        for (byte b: addressInBytes) {
            if (builder.length() > 0) {
                builder.append(":");
            }
            builder.append(String.format("%02X", b & 0xFF));
        }
        return builder.toString();
    }
}
