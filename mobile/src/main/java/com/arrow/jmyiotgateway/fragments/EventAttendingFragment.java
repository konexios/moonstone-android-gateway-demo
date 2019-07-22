package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.activities.AccountActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by batrakov on 12.01.18.
 */

public class EventAttendingFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.confirm_event_button)
    Button mConfirmEventButton;


    @BindView(R.id.deny_event_button)
    Button mDenyEventButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_event_attending, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        mConfirmEventButton.setOnClickListener(this);
        mDenyEventButton.setOnClickListener(this);
        return mRootView;
    }

    @Override
    public String getTitle(Context context) {
        return "";
    }


    @Override
    public void onClick(View aView) {
        BaseFragment fragment = null;
        FragmentManager fragmentManager = ((AppCompatActivity) mContext).
                getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        switch (aView.getId()) {
            case R.id.confirm_event_button:
                fragment = new EventSelectionFragment();
                break;
            case R.id.deny_event_button:
                Intent intent = new Intent(mContext, AccountActivity.class);
                startActivity(intent);
                break;
        }
        if (fragment != null && fragmentManager.findFragmentByTag(fragment.getFragmentTag()) == null) {
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.event_frame, fragment);
            transaction.addToBackStack(this.getTitle(mContext));
            transaction.commit();
        }
    }
}
