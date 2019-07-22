package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.cloud.iot.IotConstant;
import com.arrow.jmyiotgateway.device.DeviceCommand;
import com.arrow.jmyiotgateway.device.DeviceCommandSender;
import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;
import com.arrow.jmyiotgateway.device.DevicePropertiesStorage;
import com.arrow.jmyiotgateway.device.DeviceType;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by osminin on 6/1/2016.
 */
public abstract class BaseConfigFragment extends BaseFragment {

    @BindView(R.id.common_config_recycler)
    RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_common_config, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.setGroupVisible(R.id.menu_details_group, false);
        }
    }

    protected void init(DevicePropertiesAbstract.PropertyKeys[] values) {
        ConfigAdapter adapter = new ConfigAdapter();
        for (DevicePropertiesAbstract.PropertyKeys key : values) {
            adapter.addItem(key);
        }
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    public void saveProperty(DevicePropertiesAbstract.PropertyKeys key, Boolean value) {
        DevicePropertiesStorage.saveProperty(mContext, getSPPrefix(), key, value);
    }

    public void saveProperty(DevicePropertiesAbstract.PropertyKeys key, Integer value) {
        DevicePropertiesStorage.saveProperty(mContext, getSPPrefix(), key, value);
    }

    public boolean isChecked(DevicePropertiesAbstract.PropertyKeys propertyKey) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String key = getSPPrefix() + propertyKey.getStringKey();
        return sp.getBoolean(key, DevicePropertiesAbstract.DEFAULT_BOOLEAN_VALUE);
    }

    @Override
    public boolean onBackPressed() {
        DeviceType deviceType = Parcels.unwrap(getArguments().getParcelable(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE));
        long cardId = getArguments().getLong(IotConstant.EXTRA_DATA_LABEL_CARD_ID);
        DeviceCommandSender.sendCommand(mContext, deviceType, cardId, DeviceCommand.CommandType.PropertyChanged);
        return super.onBackPressed();
    }

    protected abstract String getSPPrefix();

    public class ConfigAdapter extends RecyclerView.Adapter<ConfigAdapter.ViewHolder> {

        private List<DevicePropertiesAbstract.PropertyKeys> mList;

        public ConfigAdapter() {
            mList = new ArrayList<>();
        }

        public void addItem(DevicePropertiesAbstract.PropertyKeys key) {
            mList.add(key);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.layout_common_config_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final DevicePropertiesAbstract.PropertyKeys key = mList.get(position);
            holder.mTelemetryName.setText(key.getStringResourceId());
            holder.mSwitch.setChecked(isChecked(key));
            holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    saveProperty(key, isChecked);
                }
            });
            if (position == getItemCount() - 1) {
                holder.mSeparator.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.config_telemetry_label)
            TextView mTelemetryName;
            @BindView(R.id.config_switch)
            SwitchCompat mSwitch;
            @BindView(R.id.separator)
            View mSeparator;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
