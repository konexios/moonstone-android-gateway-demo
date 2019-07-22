package com.arrow.jmyiotgateway.device;

import org.parceler.Parcel;

/**
 * Created by osminin on 3/21/2016.
 */

@Parcel
public class DeviceCommand {
    public static final String EXTRA_DATA_LABEL_DEVICE_COMMAND = "extra_data_polling_command";

    DeviceType mDeviceType;
    CommandType mCommandType;

    public DeviceCommand() {
    }

    public DeviceCommand(DeviceType deviceType, CommandType commandType) {
        mDeviceType = deviceType;
        mCommandType = commandType;
    }

    public DeviceType getDeviceType() {
        return mDeviceType;
    }

    public CommandType getCommandType() {
        return mCommandType;
    }

    public enum CommandType {
        Start,
        Stop,
        PropertyChanged
    }
}
