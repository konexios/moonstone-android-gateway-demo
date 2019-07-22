package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.thunderboard.TBProperties;

/**
 * Created by osminin on 8/23/2016.
 */

public final class ThunderBoardConfigFragment extends BaseConfigFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init(TBProperties.TBPropertiesKeys.values());
    }

    @Override
    protected String getSPPrefix() {
        return TBProperties.SP_PREFIX_KEY;
    }

    @Override
    public String getTitle(Context context) {
        return mContext.getString(R.string.thunder_board_config_title);
    }
}
