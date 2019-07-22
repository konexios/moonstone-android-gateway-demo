package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.arrow.acn.api.AcnApiService;
import com.arrow.acn.api.listeners.PagingResultListener;
import com.arrow.acn.api.models.ApiError;
import com.arrow.acn.api.models.DeviceEventModel;
import com.arrow.acn.api.models.HistoricalEventsRequest;
import com.arrow.acn.api.models.PagingResultModel;
import com.arrow.jmyiotgateway.AcnServiceHolder;
import com.arrow.jmyiotgateway.R;
import com.google.firebase.crash.FirebaseCrash;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arrow.jmyiotgateway.fragments.ActionsFragment.DEVICE_HID_BUNDLE;

/**
 * Created by osminin on 8/29/2016.
 */

public final class HistoricalActionsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private final static String TAG = HistoricalActionsFragment.class.getSimpleName();

    @BindView(R.id.fragment_actions_list)
    ListView mListView;
    @BindView(R.id.fragment_actions_swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.fragment_actions_history)
    View mHistoryBtn;

    private String mDeviceHid;
    private AcnApiService mRestService;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_actions, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        mHistoryBtn.setVisibility(View.GONE);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDeviceHid = getArguments().getString(DEVICE_HID_BUNDLE);
        mRestService = AcnServiceHolder.getService();
        updateEvents();
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.fragment_historical_actions);
    }

    private void updateEvents() {
        HistoricalEventsRequest request = new HistoricalEventsRequest();
        request.setHid(mDeviceHid);
        request.setSize(200);
        mRestService.getDeviceHistoricalEvents(request, new PagingResultListener<DeviceEventModel>() {
            @Override
            public void onRequestSuccess(PagingResultModel<DeviceEventModel> model) {
                if (mRootView != null) {
                    List<DeviceEventModel> data = model.getData();
                    mListView.setAdapter(new HistoricalEventsAdapter(mContext, R.layout.layout_historical_event, data));
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onRequestError(ApiError error) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "getHistoricalEvents onFailure");
                if (mRootView != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    showError(error);
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        updateEvents();
    }

    private class HistoricalEventsAdapter extends ArrayAdapter<DeviceEventModel> {

        public HistoricalEventsAdapter(Context context, int resource, List<DeviceEventModel> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.layout_historical_event, null);
            }

            DeviceEventModel data = getItem(position);

            if (data != null) {
                TextView status = ButterKnife.findById(v, R.id.historical_event_status);
                TextView name = ButterKnife.findById(v, R.id.historical_event_name);
                TextView criteria = ButterKnife.findById(v, R.id.historical_event_criteria);
                TextView date = ButterKnife.findById(v, R.id.historical_event_date);
                if (status != null) {
                    status.setText(data.getStatus());
                }
                if (name != null) {
                    name.setText(data.getDeviceActionTypeName());
                }
                if (criteria != null) {
                    criteria.setText(data.getCriteria());
                }
                if (date != null) {
                    date.setText(data.getCreatedDate());
                }
            }

            return v;
        }


    }
}
