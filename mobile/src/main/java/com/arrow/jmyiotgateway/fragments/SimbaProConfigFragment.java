package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.simbapro.SimbaProProperties;

/**
 * Created by osminin on 17.01.2018.
 */

public final class SimbaProConfigFragment extends BaseConfigFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init(SimbaProProperties.SimbaProPropertyKeys.values());
    }

    @Override
    public String getTitle(Context context) {
        return getString(R.string.simba_pro_config_title);
    }

    @Override
    protected String getSPPrefix() {
        return SimbaProProperties.SP_PREFIX_KEY;
    }
}
