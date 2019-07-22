package com.arrow.jmyiotgateway.cloud;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.arrow.acn.api.AcnApiService;
import com.arrow.acn.api.listeners.CommonRequestListener;
import com.arrow.acn.api.models.ApiError;
import com.arrow.acn.api.models.CommonResponse;
import com.arrow.acn.api.models.GatewayEventModel;
import com.arrow.jmyiotgateway.AcnServiceHolder;
import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.device.DeviceCommand;
import com.arrow.jmyiotgateway.device.DeviceCommandSender;
import com.arrow.jmyiotgateway.device.DeviceKey;
import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;
import com.arrow.jmyiotgateway.device.DevicePropertiesStorage;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.google.firebase.crash.FirebaseCrash;

import static com.arrow.acn.api.models.GatewayEventModel.DEVICE_HID_KEY;

/**
 * Created by osminin on 9/27/2016.
 */

final class ServerCommandsHandler {
    private static final String TAG = ServerCommandsHandler.class.getName();


    private AcnApiService mService;
    private Context mContext;
    private Handler mUiHandler;

    public ServerCommandsHandler(Context context) {
        mContext = context;
        mService = AcnServiceHolder.getService();
        mUiHandler = new Handler(context.getMainLooper());
    }

    public void handleServerCommand(GatewayEventModel model, Config config) {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "handleServerCommand");
        mUiHandler.post(new HandlerTask(model, config));
    }

    private boolean notifyDevicePropertyChanged(DeviceType type, long cardId) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "notifyDevicePropertyChanged()");
        return DeviceCommandSender.sendCommand(mContext, type, cardId, DeviceCommand.CommandType.PropertyChanged);
    }

    private class HandlerTask implements Runnable {
        private GatewayEventModel mModel;
        private Config mConfig;

        public HandlerTask(GatewayEventModel model, Config config) {
            this.mConfig = config;
            this.mModel = model;
        }

        @Override
        public void run() {
            final String deviceHid = mModel.getParameters().get(DEVICE_HID_KEY);
            boolean result = false;
            switch (mModel.getName()) {
                case AcnEventNames.ServerToGateway.DEVICE_START: {
                    result = DeviceCommandSender.sendCommand(mContext,
                            deviceHid, DeviceCommand.CommandType.Start, mConfig);
                    break;
                }
                case AcnEventNames.ServerToGateway.DEVICE_STOP: {
                    result = DeviceCommandSender.sendCommand(mContext, deviceHid,
                            DeviceCommand.CommandType.Stop, mConfig);
                    break;
                }
                case AcnEventNames.ServerToGateway.DEVICE_PROPERTY_CHANGE:
                    mService.registerReceivedEvent(mModel.getHid(), mCommonListener);
                    DeviceKey deviceKey = DevicePropertiesStorage.getDeviceKeyByHid(mContext,
                            deviceHid, mConfig);
                    if (deviceKey != null) {
                        DeviceType deviceType = deviceKey.getDeviceType();
                        DevicePropertiesAbstract properties = DevicePropertiesAbstract.create(mContext,
                                deviceType);
                        result = properties.saveToPreferencesJsonMap(mModel.getParameters());
                        if (result) {
                            notifyDevicePropertyChanged(deviceType, deviceKey.getIndex());
                        }
                    }
                    break;
                case AcnEventNames.ServerToGateway.STATE_CHANGED:
                    result = DeviceCommandSender.sendStateRequest(mContext, mModel, deviceHid, mConfig);
                    break;
            }
            if (result) {
                mService.eventHandlingSucceed(mModel.getHid(), mCommonListener);
                FirebaseCrash.logcat(Log.VERBOSE, TAG, "putSucceeded");
            } else {
                mService.eventHandlingFailed(mModel.getHid(), mCommonListener);
                FirebaseCrash.logcat(Log.VERBOSE, TAG, "putFailed");
            }
        }
    }

    private CommonRequestListener mCommonListener = new CommonRequestListener() {
        @Override
        public void onRequestSuccess(CommonResponse commonResponse) {
            FirebaseCrash.logcat(Log.VERBOSE, TAG, "onRequestSuccess");
        }

        @Override
        public void onRequestError(ApiError apiError) {
            FirebaseCrash.logcat(Log.VERBOSE, TAG, "onRequestError: ");
        }
    };
}
