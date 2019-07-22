package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

public class EventAccountExistFragment extends BaseFragment implements View.OnClickListener{
    @BindView(R.id.back_from_account_exists_screen_to_registration)
    Button mBackButton;

    @BindView(R.id.sign_in_from_account_exists_screen)
    Button mSignInButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_event_account_exists, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        mBackButton.setOnClickListener(this);
        mSignInButton.setOnClickListener(this);
        return mRootView;
    }

    @Override
    public String getTitle(Context context) {
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_from_account_exists_screen_to_registration:
                ((AppCompatActivity) mContext).onBackPressed();
                break;
            case R.id.sign_in_from_account_exists_screen:
                Intent intent = new Intent(mContext, AccountActivity.class);
                startActivity(intent);
                break;
        }
    }
}
