package com.arrow.jmyiotgateway.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.arrow.acn.api.AcnApiService;
import com.arrow.acn.api.listeners.PagingResultListener;
import com.arrow.acn.api.models.ApiError;
import com.arrow.acn.api.models.FindTelemetryRequest;
import com.arrow.acn.api.models.PagingResultModel;
import com.arrow.acn.api.models.TelemetryItemModel;
import com.arrow.jmyiotgateway.AcnServiceHolder;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.cloud.iot.IotConstant;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.firebase.crash.FirebaseCrash;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.arrow.jmyiotgateway.fragments.ActionsFragment.DEVICE_HID_BUNDLE;

/**
 * Created by osminin on 28.12.2016.
 */

public class TelemetryDetailsFragment extends BaseFragment implements IAxisValueFormatter {
    static final int SECONDS_IN_DAY = 3600 * 24;
    static final int DAYS_COUNT = 7;
    private static final String TAG = TelemetryDetailsFragment.class.getSimpleName();
    private static final String TIMESTAMP_LABEL = "timestamp";
    String mTelemetryName;

    @BindView(R.id.telemetry_detail_name)
    TextView mTitle;
    @BindView(R.id.telemetry_detail_from)
    TextView mFrom;
    @BindView(R.id.telemetry_detail_to)
    TextView mTo;
    @BindView(R.id.telemetry_detail_chart)
    LineChart mChart;
    @BindView(R.id.telemetry_detail_progress)
    ProgressBar mProgressBar;

    private Long mFromTimestamp;
    private Long mToTimestamp;

    private AcnApiService mRestService;
    private String mDeviceHid;
    private List<TelemetryItemModel> mData;

    private String[] mTelemetryNames;

    private static String getFormattedDateTime(Long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy k:mm");
        String formattedDate = fmt.format(date);
        return formattedDate;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDeviceHid = getArguments().getString(DEVICE_HID_BUNDLE);
        mTelemetryNames = getArguments().
                getStringArray(IotConstant.EXTRA_DATA_LABEL_DEVICE_TELEMETRY_KEYS);
        for (int i = 0; i < mTelemetryNames.length; ++i) {
            mTelemetryNames[i] = formatTelemetryName(mTelemetryNames[i]);
        }
        mRestService = AcnServiceHolder.getService();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_telemetry_details, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        initialize();
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

    @OnClick(R.id.telemetry_detail_from)
    void fromLabelClicked() {
        if (TextUtils.isEmpty(mTelemetryName)) {
            showError(mContext.getString(R.string.telemetry_error_not_selected));
            return;
        }
        showDatePicker(mFromTimestamp, new DateTimeListener() {
            @Override
            public void onDateTimeSet(Long timestamp) {
                mFromTimestamp = timestamp;
                String formattedDate = getFormattedDateTime(mFromTimestamp);
                mFrom.setText(formattedDate);
                getGraphData();
            }
        });
    }

    @OnClick(R.id.telemetry_detail_to)
    void toLabelClicked() {
        if (TextUtils.isEmpty(mTelemetryName)) {
            showError(mContext.getString(R.string.telemetry_error_not_selected));
            return;
        }
        showDatePicker(mToTimestamp, new DateTimeListener() {
            @Override
            public void onDateTimeSet(Long timestamp) {
                mToTimestamp = timestamp;
                String formattedDate = getFormattedDateTime(mToTimestamp);
                mTo.setText(formattedDate);
                getGraphData();
            }
        });
    }

    @OnClick(R.id.telemetry_detail_name)
    void onTelemetrySelect() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
        builderSingle.setTitle(R.string.telemetry_select);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.select_dialog_singlechoice);
        for (String str : mTelemetryNames) {
            arrayAdapter.add(str);
        }

        builderSingle.setNegativeButton(R.string.cast_tracks_chooser_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTelemetryName = arrayAdapter.getItem(which);
                mTitle.setText(mTelemetryName);
                dialog.dismiss();
            }
        });
        builderSingle.show();
    }

    private void initialize() {
        mFromTimestamp = System.currentTimeMillis();
        mToTimestamp = mFromTimestamp;
        String formattedDate = getFormattedDateTime(mFromTimestamp);
        mFrom.setText(formattedDate);
        mTo.setText(formattedDate);
        mChart.setScaleXEnabled(false);
        mChart.setScaleYEnabled(false);

        mChart.getAxisRight().setEnabled(false);
        mChart.getAxisLeft().setDrawAxisLine(false);

        mChart.getXAxis().setValueFormatter(this);
        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setGranularity(SECONDS_IN_DAY);
        mChart.getXAxis().setLabelCount(DAYS_COUNT, true);
        mChart.getXAxis().setTextColor(Color.WHITE);

        mChart.getAxisLeft().setTextColor(Color.WHITE);
        mChart.getLegend().setTextColor(Color.WHITE);
        mChart.setDescription(null);
    }

    private void getGraphData() {
        if (mToTimestamp > mFromTimestamp) {
            mProgressBar.setVisibility(View.VISIBLE);
            FindTelemetryRequest request = new FindTelemetryRequest();
            request.setHid(mDeviceHid);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            request.setFromTimestamp(format.format(new Date(mFromTimestamp)));
            request.setToTimestamp(format.format(new Date(mToTimestamp)));
            request.setTelemetryNames(mTelemetryName);
            request.setSize(200);
            request.setPage(0);
            mRestService.findTelemetryByDeviceHid(request, new PagingResultListener<TelemetryItemModel>() {

                @Override
                public void onRequestSuccess(PagingResultModel<TelemetryItemModel> model) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "findTelemetryByDeviceHid ok");
                    if (mRootView != null) {
                        mData = model.getData();
                        mProgressBar.setVisibility(View.GONE);
                        updateGraph();
                    }
                }

                @Override
                public void onRequestError(ApiError error) {
                    if (mRootView != null) {
                        mProgressBar.setVisibility(View.GONE);
                        showError(error);
                    }
                    FirebaseCrash.logcat(Log.ERROR, TAG, "findTelemetryByDeviceHid error: " + error.getMessage());
                }
            });
        }
    }

    private void showDatePicker(final Long timestamp, final DateTimeListener listener) {
        DatePickerFragment newFragment = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(TIMESTAMP_LABEL, timestamp);
        newFragment.setArguments(bundle);
        newFragment.setListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                showTimePicker(timestamp, year, monthOfYear, dayOfMonth, listener);
            }
        });
        newFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "datePicker");
    }

    private void showTimePicker(Long timestamp, final int year, final int monthOfYear,
                                final int dayOfMonth, final DateTimeListener listener) {
        TimePickerFragment newFragment = new TimePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(TIMESTAMP_LABEL, timestamp);
        newFragment.setArguments(bundle);
        newFragment.setListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth, hourOfDay, minute, 0);
                listener.onDateTimeSet(c.getTimeInMillis());
            }
        });
        newFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "timePicker");
    }

    private void updateGraph() {
        if (mData.size() != 0) {
            List<Entry> entries = new ArrayList<>();

            Integer i = -1;
            for (TelemetryItemModel data : mData) {
                entries.add(new Entry(++i, data.getFloatValue()));
            }
            LineDataSet dataSet = new LineDataSet(entries, mTelemetryName);
            dataSet.setColor(mContext.getResources().getColor(R.color.graph_line_color));
            dataSet.setValueTextColor(mContext.getResources().getColor(R.color.graph_line_color));
            dataSet.setDrawValues(false);
            dataSet.setLineWidth(3.0f);
            dataSet.setCircleRadius(6.0f);
            dataSet.setDrawCircleHole(false);
            LineData lineData = new LineData(dataSet);
            mChart.setData(lineData);
            mChart.invalidate();
        } else {
            mChart.clear();
        }
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int index = (int) value;
        Long timestamp = Long.parseLong(mData.get(index).getTimestamp());
        Date date = new Date(timestamp);
        SimpleDateFormat fmt = new SimpleDateFormat("k:mm:ss");
        String formattedDate = fmt.format(date);
        return formattedDate;
    }

    private String formatTelemetryName(String telemetryName) {
        int startIndex = telemetryName.indexOf("|") + 1;
        return telemetryName.substring(startIndex);
    }

    private interface DateTimeListener {
        void onDateTimeSet(Long timestamp);
    }

    public static class DatePickerFragment extends DialogFragment {

        private DatePickerDialog.OnDateSetListener mListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            Long time = bundle.getLong(TIMESTAMP_LABEL);
            final Calendar c = Calendar.getInstance();
            c.setTime(new Date(time));
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), mListener, year, month, day);
        }

        public void setListener(DatePickerDialog.OnDateSetListener listener) {
            mListener = listener;
        }
    }

    public static class TimePickerFragment extends DialogFragment {
        private TimePickerDialog.OnTimeSetListener mListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            Long time = bundle.getLong("timestamp");
            final Calendar c = Calendar.getInstance();
            c.setTime(new Date(time));
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), mListener, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void setListener(TimePickerDialog.OnTimeSetListener listener) {
            mListener = listener;
        }
    }
}
