package com.arrow.jmyiotgateway.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.arrow.acn.api.AcnApiService;
import com.arrow.jmyiotgateway.AcnServiceHolder;
import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.activities.MainActivity;
import com.arrow.jmyiotgateway.adapters.SimpleCardsAdapter;
import com.arrow.jmyiotgateway.cards.BaseCard;
import com.arrow.jmyiotgateway.custom.SwipeableRecyclerViewTouchListener;
import com.arrow.jmyiotgateway.device.DeviceAbstract;
import com.arrow.jmyiotgateway.device.DevicePollingService;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.miramonti.list.DevicesSimbaProActivitity;
import com.google.firebase.crash.FirebaseCrash;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static android.app.Activity.RESULT_OK;
import static com.arrow.jmyiotgateway.Constant.CONFIG_EXTRA_INFO;
import static com.arrow.jmyiotgateway.device.DeviceType.MicrosoftBand;
import static com.arrow.jmyiotgateway.device.DeviceType.SenseAbilityKit;
import static com.arrow.jmyiotgateway.device.DeviceType.SensorPuck;
import static com.arrow.jmyiotgateway.device.DeviceType.SimbaPro;
import static com.arrow.jmyiotgateway.device.simbapro.SimbaPro.SIMBA_PRO_MAC_EXTRA;

/**
 * Created by osminin on 3/28/2016.
 */
public class DeviceListFragment extends BaseFragment implements MainActivity.AddFabListener {
    private final static String TAG = DeviceListFragment.class.getSimpleName();
    private static final int SIMBA_PRO_LIST = 100;

    @BindView(R.id.device_list_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.device_list_name_label)
    TextView mNameLabel;

    private SimpleCardsAdapter mCardsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Config mConfig;
    private AcnApiService mRestService;
    private Subscription mClickSubscription;

    private DeviceRegisteredListener mDeviceRegisteredListener = new DeviceRegisteredListener() {
        @Override
        public void onDeviceRegistered(final String deviceHid, final long cardId, final DeviceType deviceType) {
            //TODO disable this part of code due to we don't have to get names from cloud
            /* mRestService = AcnServiceHolder.getService();
            if (mContext == null) {
                return;
            }
            if (mRestService == null) {
                mConfig = Config.loadActive(mContext);
                mRestService = AcnServiceHolder.createService(mContext.getApplicationContext(), mConfig);
            }
            mRestService.findDeviceByHid(deviceHid, new FindDeviceListener() {
                @Override
                public void onDeviceFindSuccess(DeviceModel deviceModel) {
                    if (mRootView != null && mContext != null) {
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "onDeviceFindSuccess");
                        mConfig = Config.loadActive(mContext);
                        mConfig.updateDevice(new Config.ConfigDeviceModel()
                                .setDeviceHid(deviceHid)
                                .setDeviceType(deviceType)
                                .setDeviceName(deviceModel.getName())
                                .setIndex(cardId));
                        mConfig.save(mContext);
                    }
                }

                @Override
                public void onDeviceFindFailed(ApiError apiError) {
                    FirebaseCrash.logcat(Log.ERROR, TAG, "onDeviceFindFailed");
                }
            });*/
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_device_list, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        setRetainInstance(true);
        return mRootView;
    }

    @Override
    public String getTitle(Context context) {
        return "";
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mCardsAdapter = new SimpleCardsAdapter(savedInstanceState, mContext);
        mConfig = new Config().loadActive(mContext);
        if (null == mConfig) {
            mConfig = Parcels.unwrap(getArguments().getParcelable(CONFIG_EXTRA_INFO));
        }
        mRestService = AcnServiceHolder.getService();
        loadAllCards();
        mCardsAdapter.onCreate(savedInstanceState);
        mRecyclerView.setAdapter(mCardsAdapter);
        addSwipeTouchListener();
        mClickSubscription = mCardsAdapter.getPositionClicks()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BaseCard>() {
                    @Override
                    public void call(BaseCard baseCard) {
                        if (baseCard.getDevicePollingService() != null) {
                            DeviceAbstract device = baseCard.createDevice();
                            AbstractDetailsFragment fragment = device.getDetailsFragment(baseCard.getCardId());
                            fragment.setDevicePollingService(baseCard.getDevicePollingService());
                            FragmentTransaction transaction = ((AppCompatActivity) mContext).
                                    getSupportFragmentManager().beginTransaction();
                            transaction.add(R.id.content_frame, fragment);
                            transaction.addToBackStack(fragment.getFragmentTag());
                            transaction.commit();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        FirebaseCrash.logcat(Log.ERROR, TAG, "cards adapter subscription failed");
                        FirebaseCrash.report(throwable);
                    }
                });
        if (!TextUtils.isEmpty(mConfig.getName())) {
            mNameLabel.setText(mConfig.getName());
        } else {
            mNameLabel.setText(R.string.profile);
        }
    }

    @Override
    public void update() {
        loadAllCards();
    }

    public void unbindServices() {
        int count = mCardsAdapter.getItemCount();
        for (int i =0; i < count; ++i) {
            BaseCard card = mCardsAdapter.getCard(i);
            DevicePollingService service = card.getDevicePollingService();
            service.stopPolling(card.getDeviceType(), card.getCardId());
            service.stopDevice(card.getDeviceType(), card.getCardId());
            service.removeDevice(card.getDeviceType(), card.getCardId());
            card.onDestroy();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIMBA_PRO_LIST && resultCode == RESULT_OK) {
            mConfig = Config.loadActive(mContext);
            List<Config.ConfigDeviceModel> devices = mConfig.getAddedDevices(mContext);
            long index = data.getExtras().getLong(SIMBA_PRO_MAC_EXTRA);
            for (Config.ConfigDeviceModel deviceModel : devices) {
                if (deviceModel.getDeviceType().equals(SimbaPro)) {
                    return;
                }
            }
            Config.ConfigDeviceModel newDevice = new Config.ConfigDeviceModel()
                    .setDeviceHid("")
                    .setDeviceName(getDeviceName(SimbaPro))
                    .setDeviceType(SimbaPro)
                    .setIndex(index);
            BaseCard card = getCardByType(newDevice);
            mCardsAdapter.addCard(card);
            mConfig.addDevice(newDevice);
            mConfig.save(mContext);
        }
    }

    //***************************************************************************************
    // private methods
    //***************************************************************************************

    private void addNewCard(DeviceType deviceType) {
        List<Config.ConfigDeviceModel> devices = mConfig.getAddedDevices(mContext);
        boolean isPresent = false;
        for (Config.ConfigDeviceModel deviceModel : devices) {
            if (deviceModel.getDeviceType().equals(deviceType)) {
                isPresent = true;
                break;
            }
        }
        if (!isPresent && deviceType == SimbaPro) {
            startActivityForResult(new Intent(mContext, DevicesSimbaProActivitity.class), SIMBA_PRO_LIST);
            return;
        }
        if (!isPresent || deviceType == SensorPuck) {
            Config.ConfigDeviceModel newDevice = new Config.ConfigDeviceModel()
                    .setDeviceHid("")
                    .setDeviceName(getDeviceName(deviceType))
                    .setDeviceType(deviceType)
                    .setIndex(0);
            if (deviceType == SensorPuck) {
                long index = -1;
                for (Config.ConfigDeviceModel deviceModel : devices) {
                    if (deviceModel.getDeviceType().equals(SensorPuck)
                            && deviceModel.getIndex() > index) {
                        index = deviceModel.getIndex();
                    }
                }
                newDevice.setIndex(++index);
            }
            BaseCard card = getCardByType(newDevice);
            mCardsAdapter.addCard(card);
            mConfig = Config.loadActive(mContext);
            mConfig.addDevice(newDevice);
            mConfig.save(mContext);
            FirebaseCrash.logcat(Log.INFO, TAG, "save: " + mConfig.getEmail() + mConfig.getActive());
        }
    }

    private void loadAllCards() {
        ArrayList<BaseCard> cards = new ArrayList<>();
        for (Config.ConfigDeviceModel device : mConfig.getAddedDevices(mContext)) {
            BaseCard card = getCardByType(device);
            if (card != null) {
                cards.add(card);
            }
        }
        mCardsAdapter.setCards(cards);
    }

    private BaseCard getCardByType(Config.ConfigDeviceModel model) {
        BaseCard card = new BaseCard(model, mDeviceRegisteredListener);
        return card;
    }

    private void addSwipeTouchListener() {
        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(mRecyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {

                            private void removeCard(int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    BaseCard cardToRemove = mCardsAdapter.getCard(position);
                                    mCardsAdapter.removeCard(cardToRemove, position);
                                    showUndoDeleteSnackBar(cardToRemove);
                                }
                                mCardsAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public boolean canSwipeLeft(int position) {
                                return true;
                            }

                            @Override
                            public boolean canSwipeRight(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                removeCard(reverseSortedPositions);
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                removeCard(reverseSortedPositions);
                            }
                        });

        mRecyclerView.addOnItemTouchListener(swipeTouchListener);
    }

    private void showUndoDeleteSnackBar(final BaseCard card) {
        String deviceName = getDeviceName(card.getDeviceType());
        if (mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }
        mSnackbar = Snackbar
                .make(mCoordinatorLayout, deviceName + " " + mContext.getString(R.string.device_list_card_removed),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.device_list_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCardsAdapter.discardRemoving(card);
                    }
                });

        mSnackbar.show();
        mSnackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (mContext != null && !((Activity) mContext).isFinishing()) {
                    if (mCardsAdapter.completeRemovingCard(card)) {
                        Config.ConfigDeviceModel model = new Config.ConfigDeviceModel()
                                .setDeviceHid(card.getDeviceHid())
                                .setDeviceType(card.getDeviceType())
                                .setDeviceName(card.getDeviceName())
                                .setIndex(card.getCardId());
                        completeRemoving(model);
                    }
                    mSnackbar = null;
                }
            }
        });
    }

    private void completeRemoving(Config.ConfigDeviceModel model) {
        mConfig = Config.loadActive(mContext);
        mConfig.getAddedDevices(mContext);
        mConfig.removeDevice(model);
        mConfig.save(mContext);
        FirebaseCrash.logcat(Log.INFO, TAG, "save: " + mConfig.getEmail() + mConfig.getActive());
    }

    private String getDeviceName(DeviceType type) {
        String result = "";
        switch (type) {
            case MicrosoftBand:
                result = mContext.getString(R.string.cards_ms_band_title);
                break;
            case SensorPuck:
                result = mContext.getString(R.string.cards_sensor_puck_title);
                break;
            case AndroidInternal:
                result = mContext.getString(R.string.cards_android_internal_title);
                break;
            case SenseAbilityKit:
                result = mContext.getString(R.string.cards_senseability_kit_title);
                break;
            case ThunderBoard:
                result = mContext.getString(R.string.thunderboard_details_title);
                break;
            case SensorTile:
                result = mContext.getString(R.string.cards_sensor_tile_title);
                break;
            case SimbaPro:
                result = mContext.getString(R.string.cards_simba_pro_title);
                break;
        }
        return result;
    }

    //***************************************************************************************
    // callbacks to adapter and cards
    //***************************************************************************************

    @Override
    public void onStart() {
        super.onStart();
        mCardsAdapter.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCardsAdapter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCardsAdapter.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCardsAdapter.onStop();
    }

    @Override
    public void onDestroy() {
        if (mClickSubscription != null && !mClickSubscription.isUnsubscribed()) {
            mClickSubscription.unsubscribe();
        }
        mCardsAdapter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mCardsAdapter.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mCardsAdapter.onLowMemory();
    }

    @Override
    public void addFabClicked() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
        builderSingle.setTitle("Choose device");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                mContext,
                android.R.layout.select_dialog_singlechoice);

        final HashMap<String, DeviceType> devices = new HashMap<>();

        for (DeviceType type : DeviceType.values()) {
            // exclude MsBand and SenseAbility
            if (type == MicrosoftBand || type == SenseAbilityKit) {
                continue;
            }
            String deviceName = getDeviceName(type);
            arrayAdapter.add(deviceName);
            devices.put(deviceName, type);
        }
        // cancel button
        builderSingle.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        addNewCard(devices.get(arrayAdapter.getItem(which)));
                        dialog.dismiss();
                    }
                });
        builderSingle.show();
    }

    public interface DeviceRegisteredListener {
        void onDeviceRegistered(String deviceHid, long cardId, DeviceType deviceType);
    }
}
