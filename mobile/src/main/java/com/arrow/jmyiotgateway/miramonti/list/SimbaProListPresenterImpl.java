package com.arrow.jmyiotgateway.miramonti.list;

import android.content.Context;
import android.util.Log;

import com.arrow.acn.api.AcnApiService;
import com.arrow.acn.api.models.SocialEventDevice;
import com.arrow.jmyiotgateway.AcnServiceHolder;
import com.arrow.jmyiotgateway.miramonti.device.SimbaProDevice;
import com.arrow.jmyiotgateway.miramonti.device.SimbaProScanner;
import com.arrow.jmyiotgateway.miramonti.error.SPError;
import com.google.firebase.crash.FirebaseCrash;
import com.polidea.rxandroidble.exceptions.BleScanException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.polidea.rxandroidble.exceptions.BleScanException.BLUETOOTH_CANNOT_START;
import static com.polidea.rxandroidble.exceptions.BleScanException.BLUETOOTH_DISABLED;
import static com.polidea.rxandroidble.exceptions.BleScanException.BLUETOOTH_NOT_AVAILABLE;
import static com.polidea.rxandroidble.exceptions.BleScanException.LOCATION_PERMISSION_MISSING;
import static com.polidea.rxandroidble.exceptions.BleScanException.LOCATION_SERVICES_DISABLED;

public final class SimbaProListPresenterImpl implements SimbaProListPresenter, Observer<List<SimbaProDevice>> {
    public static final long SP_DISCOVERY_TIMEOUT = 3000L;
    public static final int REQUEST_ENABLE_BT = 200;
    public static final int REQUEST_LOCATION_PERMISSION = 300;
    public static final int REQUEST_ENABLE_LOCATION = 400;

    private static final String TAG = SimbaProListPresenterImpl.class.getSimpleName();
    private static final int BACKPRESSURE_BUFFER_CAPACITY = 1000;
    private final SimbaProScanner mScanner;
    private SimbaProView mView;
    private Subscription mSubscription;
    private Subscription mNetworkSubscription;
    private final AcnApiService mService;
    private final Map<String, com.arrow.acn.api.models.SocialEventDevice> mEventDevices;

    public SimbaProListPresenterImpl(Context context) {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "SPListPresenterImpl()");
        mScanner = new SimbaProScanner(context);
        mService = AcnServiceHolder.getService();
        mEventDevices = new ConcurrentHashMap();
    }

    @Override
    public void bind(SimbaProView view) {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "bind()");
        mView = view;
    }

    @Override
    public void startScan() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "startScan()");
        mSubscription = mScanner.startObserve()
                .filter(spModel -> spModel != null)
                .onBackpressureBuffer(BACKPRESSURE_BUFFER_CAPACITY)
                .onBackpressureDrop()
                .buffer(SP_DISCOVERY_TIMEOUT, TimeUnit.MILLISECONDS)
                .flatMap(list -> Observable.from(list)
                        .distinct()
                        .map(model -> model.setPin(mEventDevices.isEmpty()  ||
                                !mEventDevices.containsKey(model.getSimbaBleDevice().getBleDevice().getMacAddress())? "" :
                                mEventDevices.get(model.getSimbaBleDevice().getBleDevice().getMacAddress()).getPinCode()))
                        .toSortedList())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

    @Override
    public void stopScan() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "stopScan()");
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        if (mNetworkSubscription != null && !mNetworkSubscription.isUnsubscribed()) {
            mNetworkSubscription.unsubscribe();
        }
    }

    @Override
    public void onDeviceSelected(SimbaProDevice device) {
        mView.showDetailsFragment(device);
    }

    @Override
    public void onScannerFunctionalityEnabled(int requestCode, boolean isEnabled) {
        if (isEnabled) {
            startScan();
        } else {
            switch (requestCode) {
                case REQUEST_ENABLE_BT:
                    mView.showError(SPError.BLE_NOT_ENABLED);
                    break;
                case REQUEST_ENABLE_LOCATION:
                    mView.showError(SPError.LOCATION_NOT_ENABLED);
                    break;
            }
        }
    }

    @Override
    public void getSocialEventsList() {
        mNetworkSubscription = mService.getSocialEventDevices()
                .subscribeOn(Schedulers.io())
                .doOnNext(response -> {
                    for (SocialEventDevice device : response) {
                        mEventDevices.put(device.getMacAddress(), device);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> FirebaseCrash.logcat(Log.VERBOSE, TAG, "getSocialEventsList()"),
                        t -> onError(t));
    }

    @Override
    public void onCompleted() {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onCompleted()");
    }

    @Override
    public void onError(Throwable e) {
        if (BleScanException.class.equals(e.getClass())) {
            BleScanException bleScanException = (BleScanException) e;
            handleBleException(bleScanException);
        } else {
            FirebaseCrash.logcat(Log.ERROR, TAG, "onError()");
            FirebaseCrash.report(e);
        }
    }

    @Override
    public void onNext(List<SimbaProDevice> list) {
        mView.updateItems(list);
    }

    private void handleBleException(BleScanException e) {
        switch (e.getReason()) {
            case BLUETOOTH_CANNOT_START:
                mView.showError(SPError.COMMON_ERROR);
                break;
            case BLUETOOTH_DISABLED:
                mView.showEnableBluetoothDialog();
                break;
            case BLUETOOTH_NOT_AVAILABLE:
                mView.showError(SPError.BLE_NOT_AVAILABLE);
                break;
            case LOCATION_PERMISSION_MISSING:
                mView.showLocationPermissionDialog();
                break;
            case LOCATION_SERVICES_DISABLED:
                mView.showEnableLocationDialog();
                break;
        }
    }
}
