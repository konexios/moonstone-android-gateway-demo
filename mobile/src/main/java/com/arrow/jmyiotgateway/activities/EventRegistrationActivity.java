package com.arrow.jmyiotgateway.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.fragments.BaseFragment;
import com.arrow.jmyiotgateway.fragments.EventAttendingFragment;
import com.arrow.jmyiotgateway.fragments.EventVerificationFragment;

/**
 * Created by batrakov on 12.01.18.
 */

public class EventRegistrationActivity extends AppCompatActivity {

    public static Config sEventConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_registration);
        sEventConfig = new Config();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        BaseFragment fragment;
        if (!preferences.contains(Constant.NAME)) {
            fragment = new EventAttendingFragment();
        } else {
            fragment = new EventVerificationFragment();
        }
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.add(R.id.event_frame, fragment);
        fragmentTransaction.commit();
    }
}
