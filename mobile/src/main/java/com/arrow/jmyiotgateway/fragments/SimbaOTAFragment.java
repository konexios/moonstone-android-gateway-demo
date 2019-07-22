package com.arrow.jmyiotgateway.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.simbapro.SimbaOtaListener;
import com.arrow.jmyiotgateway.device.simbapro.SimbaPro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SimbaOTAFragment extends BaseFragment implements SimbaOtaListener, AdapterView.OnItemSelectedListener {

    @BindView(R.id.simba_ota_spinner)
    AppCompatSpinner mFirmwareSpinner;
    @BindView(R.id.simba_ota_update)
    View mButton;

    private SimbaPro mDevice;
    private ProgressDialog mProgressDialog;
    private String mFirmwareName;
    private List<String> mAvailableFirmwares;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_simba_ota, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDevice = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AssetManager manager = mContext.getAssets();
        try {
            String[] files = manager.list("");
            mAvailableFirmwares = new ArrayList<>();
            for (String file : files) {
                if (file.endsWith(".bin")) {
                    mAvailableFirmwares.add(file);
                }
            }
            initSpinner();
            mButton.setEnabled(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDevice(SimbaPro simbaPro) {
        mDevice = simbaPro;
    }

    @Override
    public String getTitle(Context context) {
        return getString(R.string.simba_ota_title);
    }

    @OnClick(R.id.simba_ota_update)
    void upgrade() {
        mDevice.setFirmwareUpgradeListener(this);
        mDevice.upgradeFirmware(mFirmwareName);
    }

    @Override
    public void onOTAProcessStarted() {
        mProgressDialog = ProgressDialog.show(mContext, null, getString(R.string.simba_ota_updating), false);
    }

    @Override
    public void onFirmwareRead() {

    }

    @Override
    public void onOTAWriteStarted() {

    }

    @Override
    public void onOTACompleted() {
        mProgressDialog.hide();
    }

    @Override
    public void onOTAError() {
        mProgressDialog.hide();
    }

    private void initSpinner() {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(mContext, R.layout.spinner_item, mAvailableFirmwares);
        mFirmwareSpinner.setAdapter(adapter);
        Drawable spinnerDrawable = mFirmwareSpinner.getBackground().getConstantState().newDrawable();

        spinnerDrawable.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mFirmwareSpinner.setBackground(spinnerDrawable);
        } else {
            mFirmwareSpinner.setBackgroundDrawable(spinnerDrawable);
        }
        mFirmwareSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mFirmwareName = mAvailableFirmwares.get(position);
        setButtonEnabled();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        setButtonEnabled();
    }

    private void setButtonEnabled() {
        if (!TextUtils.isEmpty(mFirmwareName)) {
            mButton.setEnabled(true);
        } else {
            mButton.setEnabled(true);
        }
    }
}
