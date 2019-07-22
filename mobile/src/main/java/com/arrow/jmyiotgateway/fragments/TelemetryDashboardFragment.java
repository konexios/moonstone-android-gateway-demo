package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arrow.acn.api.AcnApiService;
import com.arrow.acn.api.listeners.PagingResultListener;
import com.arrow.acn.api.listeners.TelemetryCountListener;
import com.arrow.acn.api.models.ApiError;
import com.arrow.acn.api.models.DeviceEventModel;
import com.arrow.acn.api.models.HistoricalEventsRequest;
import com.arrow.acn.api.models.PagingResultModel;
import com.arrow.acn.api.models.TelemetryCountRequest;
import com.arrow.acn.api.models.TelemetryCountResponse;
import com.arrow.jmyiotgateway.AcnServiceHolder;
import com.arrow.jmyiotgateway.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.firebase.crash.FirebaseCrash;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static com.arrow.jmyiotgateway.fragments.ActionsFragment.DEVICE_HID_BUNDLE;
import static com.arrow.jmyiotgateway.fragments.ActionsFragment.DEVICE_NAME_BUNDLE;
import static com.arrow.jmyiotgateway.fragments.TelemetryDetailsFragment.DAYS_COUNT;
import static com.arrow.jmyiotgateway.fragments.TelemetryDetailsFragment.SECONDS_IN_DAY;

/**
 * Created by osminin on 1/17/2017.
 */

public final class TelemetryDashboardFragment extends BaseFragment implements IAxisValueFormatter {
    private static final String TAG = TelemetryDashboardFragment.class.getSimpleName();

    private static final int MILLION = 1_000_000;
    private static final int TEN_THOUSANDS = 10_000;
    private static final int ONE_THOUSAND = 1_000;

    private static final String FROM_TIME = getFormattedDateTime(getGmtTime() - DAYS_COUNT * 24 * 3600 * 1000);
    private static final String TO_TIME = getFormattedDateTime(getGmtTime());

    @BindView(R.id.dashboard_telemetries)
    TextView mTelemetry;
    @BindView(R.id.dashboard_notifications)
    TextView mNotifications;
    @BindView(R.id.telemetry_dashboard_chart)
    LineChart mChart;
    @BindView(R.id.dashboard_title)
    TextView mTitle;

    private AcnApiService mRestService;
    private String mDeviceHid;
    private List<DeviceEventModel> mEventsList = new ArrayList<>();

    private Map<Integer, Integer> mTelemetriesCount = new HashMap<>(7);

    private static String getFormattedDateTime(Long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = format.format(date).concat("T24:00:00Z");
        return formattedDate;
    }

    private static long getGmtTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        return cal.getTimeInMillis();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();

        mDeviceHid = getArguments().getString(DEVICE_HID_BUNDLE);
        String deviceName = getArguments().getString(DEVICE_NAME_BUNDLE);
        mTitle.setText(deviceName + " " + mContext.getString(R.string.dashboard_fragment_title));
        mRestService = AcnServiceHolder.getService();
        sendTelemetryRequest();
        requestNotifications(0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_telemetry_dashboard, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.device_config, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.telemetry_btn:
                BaseFragment fragment = new TelemetryDetailsFragment();
                fragment.setArguments(getArguments());
                FragmentTransaction transaction = ((AppCompatActivity) mContext).
                        getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.content_frame, fragment);
                transaction.addToBackStack(fragment.getFragmentTag());
                transaction.commit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.dashboard_fragment_title);
    }

    private void init() {
        mTelemetry.setText("0");
        mNotifications.setText("0");
        mChart.setScaleXEnabled(false);
        mChart.setScaleYEnabled(false);

        mChart.getAxisRight().setEnabled(false);
        mChart.getAxisLeft().setDrawAxisLine(true);

        mChart.getXAxis().setValueFormatter(this);
        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setGranularity(SECONDS_IN_DAY);
        mChart.getXAxis().setLabelCount(DAYS_COUNT, true);
        mChart.getXAxis().setTextColor(Color.WHITE);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawLimitLinesBehindData(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisLineColor(Color.GRAY);
        leftAxis.setGridColor(Color.GRAY);
        leftAxis.setXOffset(10f);
        mChart.getLegend().setEnabled(false);
        mChart.setDescription(null);
    }

    private void updateGraph() {
        LineDataSet telemetryDataSet = updateTelemetryChartData();
        LineDataSet notificationDataSet = updateEventsChartData();
        LineData lineData = null;
        if (telemetryDataSet != null && notificationDataSet != null) {
            lineData = new LineData(telemetryDataSet, notificationDataSet);
        } else if (telemetryDataSet != null || notificationDataSet != null) {
            lineData = new LineData(telemetryDataSet != null ? telemetryDataSet : notificationDataSet);
        }
        if (lineData != null) {
            mChart.setData(lineData);
            mChart.invalidate();
        }
    }

    private LineDataSet updateTelemetryChartData() {
        if (mTelemetriesCount != null && mTelemetriesCount.size() == DAYS_COUNT) {
            List<Entry> entries = new ArrayList<>();

            for (int i = 0; i < DAYS_COUNT; ++i) {
                entries.add(new Entry(i, mTelemetriesCount.get(i)));
            }

            LineDataSet dataSet = new LineDataSet(entries, mContext.getString(R.string.telemetry_label));
            dataSet.setColor(mContext.getResources().getColor(R.color.main_green));
            dataSet.setValueTextColor(mContext.getResources().getColor(R.color.main_green));
            dataSet.setDrawValues(false);
            dataSet.setLineWidth(0f);
            dataSet.setDrawFilled(true);
            dataSet.setDrawCircles(false);
            dataSet.setFillColor(getResources().getColor(R.color.main_green));
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            return dataSet;
        }
        return null;
    }

    private LineDataSet updateEventsChartData() {
        if (mEventsList != null && mEventsList.size() != 0) {
            List<Entry> entries = new ArrayList<>();

            List<Integer> last7Days = new ArrayList<>(Collections.nCopies(DAYS_COUNT, 0));
            Long currentTime = getGmtTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            for (DeviceEventModel data : mEventsList) {
                Date date = null;
                try {
                    int endIndex = data.getCreatedDate().indexOf('.');
                    String parcelableStr = data.getCreatedDate();
                    if (endIndex > 0) {
                        parcelableStr = parcelableStr.substring(0, endIndex);
                    }
                    date = format.parse(parcelableStr);
                } catch (ParseException e) {
                    FirebaseCrash.logcat(ERROR, TAG, "format.parse failed");
                    FirebaseCrash.report(e);
                }
                if (date != null) {
                    long diff = currentTime - date.getTime();
                    long diffInSec = diff / 1000L;
                    int diffInDays = (int) diffInSec / SECONDS_IN_DAY;
                    if (diffInDays < DAYS_COUNT) {
                        last7Days.set(DAYS_COUNT - diffInDays - 1, last7Days.get(DAYS_COUNT - diffInDays - 1) + 1);
                    }
                }
            }

            for (int i = 0; i < DAYS_COUNT; ++i) {
                entries.add(new Entry(i, last7Days.get(i)));
            }
            LineDataSet dataSet = new LineDataSet(entries, mContext.getString(R.string.notifications_label));
            dataSet.setColor(mContext.getResources().getColor(R.color.main_white));
            dataSet.setValueTextColor(mContext.getResources().getColor(R.color.main_white));
            dataSet.setDrawValues(false);
            dataSet.setLineWidth(0f);
            dataSet.setDrawFilled(true);
            dataSet.setDrawCircles(false);
            dataSet.setCircleColor(mContext.getResources().getColor(R.color.main_white));
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            return dataSet;
        }
        return null;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        SimpleDateFormat format = new SimpleDateFormat("EE");
        Date date = new Date(getGmtTime() - (int) (DAYS_COUNT - value - 1) * SECONDS_IN_DAY * 1000);
        return format.format(date);
    }

    private String getFormattedValue(int value) {
        String result;
        if (value >= MILLION) {
            result = Integer.toString(value / MILLION) + "."
                    + Integer.toString((value % MILLION) / 100_000) + "M";
        } else if (value >= TEN_THOUSANDS) {
            result = Integer.toString(value / ONE_THOUSAND) + "."
                    + Integer.toString((value % ONE_THOUSAND) / 100) + "K";
        } else {
            result = Integer.toString(value);
        }
        return result.replace(".0", "");
    }

    private void sendTelemetryRequest() {
        long currentTime = getGmtTime();

        for (int i = 0; i < DAYS_COUNT; ++i) {
            TelemetryCountRequest request = new TelemetryCountRequest();
            request.setDeviceHid(mDeviceHid);
            request.setFromTimestamp(getFormattedDateTime(currentTime - (DAYS_COUNT - i) * 24 * 3600 * 1000));
            request.setToTimestamp(getFormattedDateTime(currentTime - (DAYS_COUNT - 1 - i) * 24 * 3600 * 1000));
            request.setTelemetryName("*");
            final Integer index = i;
            mRestService.getTelemetryItemsCount(request, new TelemetryCountListener() {
                @Override
                public void onTelemetryItemsCountSuccess(TelemetryCountResponse response) {
                    if (mRootView != null) {
                        mTelemetriesCount.put(index, Integer.parseInt(response.getValue()));
                        if (mTelemetriesCount.size() == DAYS_COUNT) {
                            int totalCount = 0;
                            for (Map.Entry<Integer, Integer> entry : mTelemetriesCount.entrySet()) {
                                totalCount += entry.getValue();
                            }
                            mTelemetry.setText(getFormattedValue(totalCount));
                            updateGraph();
                        }
                    }
                }

                @Override
                public void onTelemetryItemsCountError(ApiError error) {
                    showError(error);
                }
            });
        }
    }

    private void requestNotifications(int page) {
        HistoricalEventsRequest request = new HistoricalEventsRequest();
        request.setHid(mDeviceHid);
        request.setCreatedDateFrom(FROM_TIME);
        request.setCreatedDateTo(TO_TIME);
        request.setSize(200);
        request.setPage(page);
        mRestService.getDeviceHistoricalEvents(request, new PagingResultListener<DeviceEventModel>() {
            @Override
            public void onRequestSuccess(PagingResultModel<DeviceEventModel> response) {
                FirebaseCrash.logcat(DEBUG, TAG, "events onRequestSuccess");
                if (mRootView != null) {
                    if (response.getPage().equals(response.getTotalPages())) {
                        mEventsList.addAll(response.getData());
                        mNotifications.setText(getFormattedValue(mEventsList.size()));
                        updateGraph();
                    } else {
                        mEventsList.addAll(response.getData());
                        requestNotifications(response.getPage() + 1);
                    }
                }
            }

            @Override
            public void onRequestError(ApiError error) {
                showError(error);
            }
        });
    }
}
