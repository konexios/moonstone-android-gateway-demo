package com.arrow.jmyiotgateway.cards;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.cloud.iot.IotConstant;
import com.arrow.jmyiotgateway.device.DeviceAbstract;
import com.arrow.jmyiotgateway.device.DevicePollingService;
import com.arrow.jmyiotgateway.device.DeviceState;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.fragments.DeviceListFragment;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseCard {

    protected RecyclerView.ViewHolder mViewHolder;
    protected Context mContext;
    protected Bundle mSavedInstanceState;
    private PollingServiceConnection mConnection = new PollingServiceConnection();
    private DevicePollingService mPollingService;
    private Config.ConfigDeviceModel mDeviceModel;
    private DeviceListFragment.DeviceRegisteredListener mListener;
    //Broadcast receiver
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.ACTION_IOT_DATA_RECEIVED) ||
                    intent.getAction().equals(Constant.ACTION_IOT_DEVICE_STATE_CHANGED) ||
                    intent.getAction().equals(Constant.ACTION_IOT_DEVICE_REGISTERED)) {
                handleIncomingIotData(intent);
            }
        }
    };

    public BaseCard(Config.ConfigDeviceModel deviceModel,
                    DeviceListFragment.DeviceRegisteredListener listener) {
        mDeviceModel = deviceModel;
        mListener = listener;
    }

    public long getCardId() {
        return mDeviceModel.getIndex();
    }

    public DeviceAbstract createDevice() {
        return mPollingService.createDevice(getDeviceType(), mDeviceModel.getIndex(),
                mDeviceModel.getDeviceHid());
    }

    public DevicePollingService getDevicePollingService() {
        return mPollingService;
    }

    public final void bind(RecyclerView.ViewHolder viewHolder, Context context) {
        if (mContext == null && mViewHolder == null) {
            mViewHolder = viewHolder;
            mContext = context;
            IntentFilter filter = new IntentFilter(Constant.ACTION_IOT_DATA_RECEIVED);
            filter.addAction(Constant.ACTION_IOT_DEVICE_STATE_CHANGED);
            filter.addAction(Constant.ACTION_IOT_DEVICE_REGISTERED);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
            Intent intent = new Intent(mContext, DevicePollingService.class);
            mContext.getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            ((ViewHolder) viewHolder).mTitle.setText(getDeviceName());
            if (getDeviceType().name().equals("SimbaPro")) {
                ((ViewHolder) mViewHolder).mType.setText(R.string.cards_simba_pro_title);
            } else {
                ((ViewHolder) mViewHolder).mType.setText(getDeviceType().name());
            }

        }
        if (mSavedInstanceState != null) {
            onCreate(mSavedInstanceState);
        }
    }

    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType, Bundle bundle) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.common_card, parent,
                false);
        ViewHolder viewHolder = new BaseCard.ViewHolder(v, bundle);
        return viewHolder;
    }

    public DeviceType getDeviceType() {
        return mDeviceModel.getDeviceType();
    }

    public String getDeviceName() {
        return mDeviceModel.getDeviceName();
    }

    public String getDeviceHid() {
        return mDeviceModel.getDeviceHid();
    }

    public boolean isInitialized() {
        return mViewHolder != null;
    }

    protected final void handleIncomingIotData(Intent intent) {
        DeviceType deviceType = null;
        long cardId = -1;
        boolean setChecked = false;
        if (intent.getAction().equals(Constant.ACTION_IOT_DATA_RECEIVED)) {
            setChecked = true;
            Bundle bundle = intent.getBundleExtra(IotConstant.EXTRA_DATA_LABEL_TELEMETRY_BUNDLE);
            cardId = bundle.getLong(IotConstant.EXTRA_DATA_LABEL_CARD_ID);
            deviceType = Parcels.unwrap(bundle.getParcelable(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE));
        } else if (intent.getAction().equals(Constant.ACTION_IOT_DEVICE_STATE_CHANGED)) {
            DeviceState deviceState = Parcels.unwrap(intent.getParcelableExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_STATE));
            deviceType = Parcels.unwrap(intent.getParcelableExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE));
            cardId = intent.getLongExtra(IotConstant.EXTRA_DATA_LABEL_CARD_ID, -1);
            setChecked = deviceState != DeviceState.Disconnected &&
                    deviceState != DeviceState.Bound &&
                    deviceState != DeviceState.Disconnecting &&
                    deviceState != DeviceState.Error;
        } else if (intent.getAction().equals(Constant.ACTION_IOT_DEVICE_REGISTERED)) {
            deviceType = Parcels.unwrap(intent.getParcelableExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE));
            String deviceHid = Parcels.unwrap(intent.getParcelableExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_ID));
            cardId = intent.getLongExtra(IotConstant.EXTRA_DATA_LABEL_CARD_ID, -1);
            if (mDeviceModel.getDeviceType().equals(deviceType) && cardId == mDeviceModel.getIndex()) {
                mListener.onDeviceRegistered(deviceHid, cardId, deviceType);
            }
        }
        if (deviceType == getDeviceType() && mDeviceModel.getIndex() == cardId) {
            ((ViewHolder) mViewHolder).mOnlineLabel.setVisibility(setChecked ? View.VISIBLE : View.GONE);
        }
    }

    public final int getViewType() {
        return getDeviceType().ordinal();
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            boolean isVisible = savedInstanceState.getBoolean(ViewHolder.VIEW_HOLDER_STATE + getDeviceType());
            if (isInitialized()) {
                ((ViewHolder) mViewHolder).mOnlineLabel.setVisibility(isVisible ? View.VISIBLE : View.GONE);
                mSavedInstanceState = null;
            } else {
                mSavedInstanceState = savedInstanceState;
            }
        }
    }

    public void onDestroy() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
        if (mConnection != null && mConnection.isConnected) {
            mContext.getApplicationContext().unbindService(mConnection);
            mConnection.isConnected = false;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        boolean isVisible = ((ViewHolder) mViewHolder).mOnlineLabel.getVisibility() == View.VISIBLE;
        outState.putBoolean(ViewHolder.VIEW_HOLDER_STATE + getDeviceType(), isVisible);
    }

    //To keep data, if viewholder is not initialized
    public void onSaveNonInitInstanceState(Bundle cachedState, Bundle outState) {
        if (cachedState != null) {
            outState.putBoolean(ViewHolder.VIEW_HOLDER_STATE + getDeviceType(), cachedState.getBoolean(
                    ViewHolder.VIEW_HOLDER_STATE + getDeviceType()));
        }
    }

    public void onLowMemory() {
    }

    public void onPause() {
    }

    public void onResume() {
    }

    public void update(Bundle bundle) {
    }

    public void onCardRemoved() {
        ((ViewHolder) mViewHolder).mOnlineLabel.setVisibility(View.GONE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseCard baseCard = (BaseCard) o;

        return mDeviceModel.getIndex() == baseCard.mDeviceModel.getIndex() && getDeviceType() == baseCard.getDeviceType();

    }

    @Override
    public int hashCode() {
        return (int) (mDeviceModel.getIndex() ^ (mDeviceModel.getIndex() >>> 32)) + getDeviceType().ordinal();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        static final String VIEW_HOLDER_STATE = "VIEW_HOLDER_STATE";

        @BindView(R.id.card_online_label)
        View mOnlineLabel;
        @BindView(R.id.card_title)
        TextView mTitle;
        @BindView(R.id.card_type)
        TextView mType;


        public ViewHolder(View itemView, Bundle savedState) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (savedState != null) {
                //TODO: handle saved state
            }
        }
    }

    private class PollingServiceConnection implements ServiceConnection {

        boolean isConnected;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DevicePollingService.LocalBinder binder = (DevicePollingService.LocalBinder) service;
            mPollingService = binder.getService();
            isConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPollingService = null;
            isConnected = false;
        }
    }
}
