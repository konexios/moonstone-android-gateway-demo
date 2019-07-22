package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.cloud.iot.IotConstant;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arrow.jmyiotgateway.fragments.ActionsFragment.DEVICE_HID_BUNDLE;

/**
 * Created by osminin on 12/26/2016.
 */

public final class TelemetriesFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    @BindView(R.id.fragment_telemetries_list)
    ListView mListView;
    private String[] mTelemetryNames;
    private String mDeviceHid;
    private ArrayAdapter<String> mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDeviceHid = getArguments().getString(DEVICE_HID_BUNDLE);
        mTelemetryNames = getArguments().
                getStringArray(IotConstant.EXTRA_DATA_LABEL_DEVICE_TELEMETRY_KEYS);
        for (int i = 0; i < mTelemetryNames.length; ++i) {
            mTelemetryNames[i] = formatTelemetryName(mTelemetryNames[i]);
        }
        mAdapter = new TelemetryAdapter(mContext, R.layout.layout_telemetry_item, mTelemetryNames);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_telemetries, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.setGroupVisible(R.id.menu_details_group, false);
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.telemetries_fragment_title);
    }

    private String formatTelemetryName(String telemetryName) {
        int startIndex = telemetryName.indexOf("|") + 1;
        return telemetryName.substring(startIndex);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BaseFragment fragment = new TelemetryDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_HID_BUNDLE, mDeviceHid);
        bundle.putString(IotConstant.EXTRA_DATA_LABEL_DEVICE_TELEMETRY_KEYS, mTelemetryNames[position]);
        fragment.setArguments(bundle);
        FragmentTransaction transaction = ((AppCompatActivity) mContext).
                getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.content_frame, fragment);
        transaction.addToBackStack(fragment.getFragmentTag());
        transaction.commit();
    }

    private class TelemetryAdapter extends ArrayAdapter<String> {

        public TelemetryAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.layout_telemetry_item, null);
            }
            String telemetryName = getItem(position);

            if (telemetryName != null) {
                TextView telemetryTitle = ButterKnife.findById(v, R.id.telemetry_item_title);
                telemetryTitle.setText(telemetryName);

            }
            return v;
        }
    }
}
