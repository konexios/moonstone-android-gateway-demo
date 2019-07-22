package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.msband.MsBandProperties;

/**
 * Created by osminin on 5/30/2016.
 */
public class MsBandConfigFragment extends BaseConfigFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init(MsBandProperties.MsBandPropertiesKeys.values());
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.ms_band_config_title);
    }

    @Override
    protected String getSPPrefix() {
        return MsBandProperties.SP_PREFIX_KEY;
    }
}
