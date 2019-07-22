package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.arrow.acn.api.AcnApiService;
import com.arrow.acn.api.listeners.ListResultListener;
import com.arrow.acn.api.listeners.UpdateDeviceActionListener;
import com.arrow.acn.api.models.ApiError;
import com.arrow.acn.api.models.CommonResponse;
import com.arrow.acn.api.models.DeviceActionModel;
import com.arrow.jmyiotgateway.AcnServiceHolder;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.activities.MainActivity;
import com.arrow.jmyiotgateway.device.TelemetriesNames;
import com.google.firebase.crash.FirebaseCrash;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by osminin on 8/2/2016.
 */

public final class ActionsFragment extends BaseFragment implements AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, MainActivity.AddFabListener {
    final static String ACTION_BUNDLE = "action_extra_bundle";
    final static String DEVICE_HID_BUNDLE = "device_hid_extra_bundle";
    final static String DEVICE_NAME_BUNDLE = "device_name_extra_bundle";
    private final static String TAG = ActionsFragment.class.getSimpleName();
    @BindView(R.id.fragment_actions_list)
    ListView mListView;
    @BindView(R.id.fragment_actions_swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private AcnApiService mRestService;
    private String mDeviceHid;
    private List<DeviceActionModel> mActions;
    private ArrayAdapter<DeviceActionModel> mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDeviceHid = getArguments().getString(TelemetriesNames.DEVICE_HID);
        mRestService = AcnServiceHolder.getService();
        if (mActions == null) {
            updateList();
        }
        ImageView addBtn = ButterKnife.findById(mRootView.getRootView(), R.id.main_fab_add);
        if (addBtn != null) {
            addBtn.setImageResource(R.drawable.fa_plus);
        }
    }

    @Override
    public void onDestroyView() {
        ImageView addBtn = ButterKnife.findById(mRootView.getRootView(), R.id.main_fab_add);
        addBtn.setImageResource(R.drawable.im_pencil);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        View fabAdd = ButterKnife.findById(mRootView.getRootView(), R.id.main_fab_add);
        fabAdd.setVisibility(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_actions, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        return mRootView;
    }

    @Override
    public String getTitle(Context context) {
        return mContext.getString(R.string.fragment_actions);
    }

    @OnClick(R.id.fragment_actions_history)
    public void onActionHistory() {
        showHistoricalActionsFragment();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceActionModel action = mAdapter.getItem(position);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ACTION_BUNDLE, action);
        showNewActionFragment(bundle, R.id.content_frame);
    }

    private void showHistoricalActionsFragment() {
        BaseFragment fragment = new HistoricalActionsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_HID_BUNDLE, mDeviceHid);
        fragment.setArguments(bundle);
        showFragment(fragment, R.id.content_frame);
    }

    private void showNewActionFragment(Bundle bundle, int position) {
        BaseFragment fragment = new NewActionFragment();
        if (bundle != null) {
            bundle.putString(DEVICE_HID_BUNDLE, mDeviceHid);
            fragment.setArguments(bundle);
        }
        showFragment(fragment, position);
    }

    private void showFragment(BaseFragment fragment, int container) {
        FragmentManager fragmentManager = ((AppCompatActivity) mContext).
                getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .remove(this)
                .commit();
        fragmentManager.popBackStack();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(container, fragment);
        transaction.addToBackStack(fragment.getFragmentTag());
        transaction.commit();
    }

    @Override
    public void update() {
        updateList();
    }

    private void updateList() {
        mRestService.getDeviceActions(mDeviceHid, new ListResultListener<DeviceActionModel>() {
            @Override
            public void onRequestSuccess(List<DeviceActionModel> list) {
                if (mRootView != null) {
                    mActions = list;
                    mAdapter = new ActionsAdapter(mContext, R.layout.layout_action_item, mActions);
                    mListView.setAdapter(mAdapter);
                    mListView.setOnItemClickListener(ActionsFragment.this);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onRequestError(ApiError error) {
                if (mRootView != null) {
                    FirebaseCrash.logcat(Log.ERROR, TAG, "getActions error");
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        updateList();
    }

    @Override
    public void addFabClicked() {
        showNewActionFragment(new Bundle(), R.id.content_frame);
    }

    private class ActionsAdapter extends ArrayAdapter<DeviceActionModel> implements CompoundButton.OnCheckedChangeListener {

        public ActionsAdapter(Context context, int resource, List<DeviceActionModel> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.layout_action_item, null);
            }

            DeviceActionModel actionModel = getItem(position);

            if (actionModel != null) {
                TextView actionTitle = ButterKnife.findById(v, R.id.action_title);
                TextView description = ButterKnife.findById(v, R.id.action_description);
                SwitchCompat switchCompat = ButterKnife.findById(v, R.id.action_switch);
                if (actionTitle != null) {
                    actionTitle.setText(actionModel.getSystemName());
                }
                if (description != null) {
                    description.setText(actionModel.getDescription());
                }
                if (switchCompat != null) {
                    switchCompat.setChecked(actionModel.getEnabled());
                    switchCompat.setOnCheckedChangeListener(this);
                    switchCompat.setTag(actionModel);
                }
            }

            return v;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            DeviceActionModel actionModel = (DeviceActionModel) buttonView.getTag();
            actionModel.setEnabled(isChecked);
            mRestService.updateDeviceAction(mDeviceHid, actionModel.getIndex(), actionModel, new UpdateDeviceActionListener() {

                @Override
                public void onDeviceActionUpdated(CommonResponse commonResponse) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "updateAction response");
                }

                @Override
                public void onDeviceActionUpdateFailed(ApiError apiError) {
                    FirebaseCrash.logcat(Log.ERROR, TAG, "updateAction error");
                }
            });
        }
    }
}