package com.arrow.jmyiotgateway.miramonti.list;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.activities.ActivityAbstract;
import com.arrow.jmyiotgateway.fragments.BaseFragment;
import com.google.firebase.crash.FirebaseCrash;

import butterknife.ButterKnife;

public class DevicesSimbaProActivitity extends ActivityAbstract {

    private static final String TAG = DevicesSimbaProActivitity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onCreate");
        setContentView(R.layout.activity_simba_pro);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        hideImageLogo();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        BaseFragment fragment = new SimbaProListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.simba_content_frame, fragment);
        transaction.commit();
    }
}
