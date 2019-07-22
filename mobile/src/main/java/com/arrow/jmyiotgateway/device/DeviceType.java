package com.arrow.jmyiotgateway.device;

import org.parceler.Parcel;

@Parcel
public enum DeviceType {
    MicrosoftBand,
    SensorPuck,
    AndroidInternal,
    SenseAbilityKit,
    ThunderBoard,
    SensorTile,
    SimbaPro;

    public static DeviceType getDeviceTypeByName(String name) {
        DeviceType type = null;
        if (MicrosoftBand.name().equals(name)) {
            type = MicrosoftBand;
        } else if (SensorPuck.name().equals(name)) {
            type = SensorPuck;
        } else if (AndroidInternal.name().equals(name)) {
            type = AndroidInternal;
        } else if (SenseAbilityKit.name().equals(name)) {
            type = SenseAbilityKit;
        } else if (ThunderBoard.name().equals(name)) {
            type = ThunderBoard;
        } else if (SensorTile.name().equals(name)) {
            type = SensorTile;
        } else if (SimbaPro.name().equals(name)) {
            type = SimbaPro;
        }
        return type;
    }
}
