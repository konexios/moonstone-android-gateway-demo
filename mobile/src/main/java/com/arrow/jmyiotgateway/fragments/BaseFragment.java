package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.View;

import com.arrow.acn.api.models.ApiError;
import com.arrow.jmyiotgateway.R;
import com.google.firebase.crash.FirebaseCrash;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by osminin on 3/28/2016.
 */
public abstract class BaseFragment extends Fragment {

    protected View mRootView;
    protected Context mContext;
    protected Snackbar mSnackbar;
    protected CoordinatorLayout mCoordinatorLayout;
    protected Unbinder mUnbinder;

    public String getFragmentTag() {
        return this.getClass().getName().toString();
    }

    public abstract String getTitle(Context context);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCoordinatorLayout = ButterKnife.findById(mRootView.getRootView(), R.id.main_coordinator);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRootView = null;
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    public boolean onBackPressed(){return false;}

    public void update() {}

    protected void showError(ApiError apiError) {
        if (mRootView != null && mContext != null) {
            if (mSnackbar != null && mSnackbar.isShown()) {
                mSnackbar.dismiss();
            }
            String message = getString(R.string.request_error) + ": " + apiError.getStatus()
                    + ", " + apiError.getMessage();
            mSnackbar = Snackbar
                    .make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG);
            mSnackbar.show();
        }
        FirebaseCrash.report(new Throwable(apiError.getStatus()
                + ", " + apiError.getMessage()));
    }

    protected void showError(String onlyMessage) {
        if (mRootView != null && mContext != null) {
            if (mSnackbar != null && mSnackbar.isShown()) {
                mSnackbar.dismiss();
            }
            mSnackbar = Snackbar
                    .make(mCoordinatorLayout, onlyMessage, Snackbar.LENGTH_LONG);
            mSnackbar.show();
        }
        FirebaseCrash.report(new Throwable(onlyMessage));
    }
}
