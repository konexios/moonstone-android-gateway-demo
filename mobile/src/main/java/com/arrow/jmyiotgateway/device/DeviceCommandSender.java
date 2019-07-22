package com.arrow.jmyiotgateway.device;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.arrow.acn.api.models.GatewayEventModel;
import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.cloud.iot.IotConstant;

import org.parceler.Parcels;

import static com.arrow.jmyiotgateway.cloud.iot.IotConstant.EXTRA_DATA_LABEL_CARD_ID;
import static com.arrow.jmyiotgateway.cloud.iot.IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE;

/**
 * Created by osminin on 4/29/2016.
 */
public class DeviceCommandSender {

    public static boolean sendCommand(Context context, DeviceType deviceType, long cardId,
                                      DeviceCommand.CommandType commandType) {
        Intent deviceCommandIntent = new Intent(context, DevicePollingService.class);
        DeviceCommand command = new DeviceCommand(deviceType, commandType);
        deviceCommandIntent.putExtra(DeviceCommand.EXTRA_DATA_LABEL_DEVICE_COMMAND, Parcels.wrap(command));
        deviceCommandIntent.putExtra(EXTRA_DATA_LABEL_CARD_ID, cardId);
        return context.startService(deviceCommandIntent) != null;
    }

    public static boolean sendCommand(Context context, String deviceHid,
                                      DeviceCommand.CommandType commandType,
                                      Config config) {
        DeviceKey key = DevicePropertiesStorage.getDeviceKeyByHid(context, deviceHid, config);
        if (key != null) {
            return sendCommand(context, key.getDeviceType(), key.getIndex(), commandType);
        }
        return false;
    }

    public static boolean sendStateRequest(Context context, GatewayEventModel model,
                                           String deviceHid, Config config) {
        DeviceKey key = DevicePropertiesStorage.getDeviceKeyByHid(context, deviceHid, config);
        if (key != null) {
            Intent intent = new Intent(Constant.ACTION_IOT_DEVICE_STATE_REQUEST);
            intent.putExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_STATES, model);
            intent.putExtra(EXTRA_DATA_LABEL_DEVICE_TYPE, Parcels.wrap(key.getDeviceType()));
            intent.putExtra(EXTRA_DATA_LABEL_CARD_ID, key.getIndex());
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            return true;
        }
        return false;
    }
}
