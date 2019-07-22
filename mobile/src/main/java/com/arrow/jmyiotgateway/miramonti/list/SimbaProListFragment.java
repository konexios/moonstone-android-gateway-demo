package com.arrow.jmyiotgateway.miramonti.list;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arrow.acn.api.models.ApiError;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.simbapro.SimbaProUtils;
import com.arrow.jmyiotgateway.fragments.BaseFragment;
import com.arrow.jmyiotgateway.miramonti.device.SimbaProDevice;
import com.arrow.jmyiotgateway.miramonti.error.SPError;
import com.google.firebase.crash.FirebaseCrash;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import static android.app.Activity.RESULT_OK;
import static com.arrow.jmyiotgateway.device.simbapro.SimbaPro.SIMBA_PRO_MAC_EXTRA;

public final class SimbaProListFragment extends BaseFragment implements SimbaProView, Observer<SimbaProDevice> {
    private static final int CLICK_TIMEOUT = 500;
    private static final String TAG = SimbaProListFragment.class.getName();

    @BindView(R.id.sp_list)
    RecyclerView mRecyclerView;

    private List<SimbaProDevice> mDevices;
    private SPAdapter mAdapter;
    private Subscription mAdapterSubscription;
    private SimbaProListPresenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_simba_list, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        initList();

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter = new SimbaProListPresenterImpl(getActivity());
        mPresenter.bind(this);
        mPresenter.getSocialEventsList();
    }

    @Override
    public void onDestroyView() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onDestroyView");
        super.onDestroyView();
        mPresenter.bind(null);
    }

    @Override
    public void onStart() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onStart");
        super.onStart();
        if (!isErrorShown()) {
            mPresenter.startScan();
        }
    }

    @Override
    public void onStop() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onStop");
        super.onStop();
        mPresenter.stopScan();
        mDevices.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onDestroy");
        super.onDestroy();
        mAdapterSubscription.unsubscribe();
    }

    @Override
    public String getTitle(Context context) {
        return getString(R.string.simba_list_title);
    }

    @Override
    public void showDetailsFragment(SimbaProDevice model) {
        Intent intent = new Intent();
        intent.putExtra(SIMBA_PRO_MAC_EXTRA, SimbaProUtils.macAddressToLong(model.
                getSimbaBleDevice().getBleDevice().getMacAddress()));
        ((Activity) mContext).setResult(RESULT_OK, intent);
        ((Activity) mContext).finish();
    }

    @Override
    public void updateItems(List<SimbaProDevice> list) {
        mDevices = list;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showEnableBluetoothDialog() {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "showEnableBluetoothDialog");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, SimbaProListPresenterImpl.REQUEST_ENABLE_BT);
    }

    @Override
    public void showLocationPermissionDialog() {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "showLocationPermissionDialog");
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                SimbaProListPresenterImpl.REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void showEnableLocationDialog() {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "showEnableLocationDialog");
        Intent enableBtIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(enableBtIntent, SimbaProListPresenterImpl.REQUEST_ENABLE_LOCATION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "showEnableBluetoothDialog");
        if (requestCode == SimbaProListPresenterImpl.REQUEST_ENABLE_BT) {
            mPresenter.onScannerFunctionalityEnabled(requestCode, resultCode == RESULT_OK);
        } else if (requestCode == SimbaProListPresenterImpl.REQUEST_ENABLE_LOCATION) {
            try {
                int isGpsOff = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE);
                mPresenter.onScannerFunctionalityEnabled(requestCode, isGpsOff != 0);
            } catch (Settings.SettingNotFoundException e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "onActivityResult");
                FirebaseCrash.report(e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onRequestPermissionsResult");
        if (grantResults != null && grantResults.length != 0 &&
                requestCode == SimbaProListPresenterImpl.REQUEST_LOCATION_PERMISSION) {
            mPresenter.onScannerFunctionalityEnabled(requestCode,
                    grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }


    @Override
    public void showError(SPError error) {
        if (mRootView != null && mContext != null) {
            if (mSnackbar != null && mSnackbar.isShown()) {
                mSnackbar.dismiss();
            }
            int resId = 0;
            switch (error) {
                case CONNECTION_LOST:
                    resId = R.string.error_connection_lost;
                    break;
                case BLE_NOT_AVAILABLE:
                    resId = R.string.error_ble_not_available;
                    break;
                case LOCATION_NOT_ENABLED:
                    resId = R.string.error_location_not_enabled;
                    break;
                case BLE_NOT_ENABLED:
                    resId = R.string.error_ble_not_enabled;
                    break;
                case COMMON_ERROR:
                    resId = R.string.error_common;
                    break;
            }
            showError(getString(resId));
        }
    }

    @Override
    public void showError(ApiError error) {
        showError(error);
    }

    private void initList() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onViewCreated");
        mDevices = new LinkedList<>();
        mAdapter = new SPAdapter();
        RecyclerView.LayoutManager layoutManager;
        layoutManager = new GridLayoutManager(getContext(), 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapterSubscription = mAdapter.getPositionClicks()
                .throttleFirst(CLICK_TIMEOUT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    protected boolean isErrorShown() {
        return mSnackbar != null && mSnackbar.isShown();
    }

    @Override
    public void onCompleted() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onCompleted");
    }

    @Override
    public void onError(Throwable e) {
        FirebaseCrash.logcat(Log.ERROR, TAG, "onError");
    }

    @Override
    public void onNext(SimbaProDevice simbaProDevice) {
        String msg = TextUtils.isEmpty(simbaProDevice.getPin()) ?
                simbaProDevice.getSimbaBleDevice().getBleDevice().getMacAddress() :
                simbaProDevice.getPin();
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_simba_confirmation_title)
                .setMessage(String.format("%s %s?", getString(R.string.add_simba_confirmation), msg))
                .setPositiveButton(R.string.yes, (dialog, which) -> mPresenter.onDeviceSelected(simbaProDevice))
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    class SPAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final PublishSubject<SimbaProDevice> mOnClickSubject = PublishSubject.create();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sp_card_layout, parent,
                    false);
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.height = mRecyclerView.getWidth() / 2;
            v.setLayoutParams(lp);
            return new SPViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final SimbaProDevice model = mDevices.get(position);
            if (TextUtils.isEmpty(model.getPin())) {
                ((SPViewHolder) holder).mCardFirstLine.setText(model.getSimbaBleDevice().getBleDevice().getMacAddress());
                ((SPViewHolder) holder).mCardSecondLine.setVisibility(View.GONE);
            } else {
                ((SPViewHolder) holder).mCardFirstLine.setText(model.getPin());
                ((SPViewHolder) holder).mCardSecondLine.setVisibility(View.VISIBLE);
                ((SPViewHolder) holder).mCardSecondLine.setText(model.getSimbaBleDevice().getBleDevice().getMacAddress());
            }
            holder.itemView.setOnClickListener(view -> mOnClickSubject.onNext(model));
        }

        @Override
        public long getItemId(int position) {
            return mDevices.get(position).hashCode();
        }

        @Override
        public int getItemCount() {
            return mDevices == null ? 0 : mDevices.size();
        }

        public Observable<SimbaProDevice> getPositionClicks() {
            return mOnClickSubject.asObservable();
        }

        class SPViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.sp_card_first)
            TextView mCardFirstLine;
            @BindView(R.id.sp_card_second)
            TextView mCardSecondLine;

            public SPViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
