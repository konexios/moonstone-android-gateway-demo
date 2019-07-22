package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.sensortile.SensorTileProperties;

/**
 * TODO: Add a class header comment!
 */

public final class SensorTileConfigFragment extends BaseConfigFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init(SensorTileProperties.SensorTilePropertyKeys.values());
    }

    @Override
    protected String getSPPrefix() {
        return SensorTileProperties.SP_PREFIX_KEY;
    }

    @Override
    public String getTitle(Context context) {
        return mContext.getString(R.string.sensor_tile_config_title);
    }
}
