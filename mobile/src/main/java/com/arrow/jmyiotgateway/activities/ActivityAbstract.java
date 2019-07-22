package com.arrow.jmyiotgateway.activities;

import android.app.ProgressDialog;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.fragments.BaseFragment;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class ActivityAbstract extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {
    protected static final String TITLE_EXTRA_BUNDLE = "title";
    private final static String TAG = ActivityAbstract.class.getSimpleName();
    protected String mTitle;
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mToolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_24dp);
    }

    @Override
    public void onBackPressed() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onBackPressed");
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.executePendingTransactions();
        if (fragmentManager.getFragments() != null) {
            int count = fragmentManager.getFragments().size();
            BaseFragment lastFragment = (BaseFragment) fragmentManager.getFragments().get(count - 1);
            if (lastFragment == null || !lastFragment.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onBackStackChanged");
        BaseFragment lastFragment = getLastBaseFragment();
        if (lastFragment != null) {
            mToolbar.setTitle(lastFragment.getTitle(this));
        } else {
            String title = TextUtils.isEmpty(mTitle) ? "" : mTitle;
            mToolbar.setTitle(title);
        }
        lastFragment.update();
        setBurgerButtonState();
        int count = getSupportFragmentManager().getBackStackEntryCount();
        showBottomSheet(count != 0);
        showFabButton(lastFragment instanceof MainActivity.AddFabListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onSaveInstanceState");
        CharSequence title = mToolbar.getTitle();
        outState.putCharSequence(TITLE_EXTRA_BUNDLE, title);
        super.onSaveInstanceState(outState);
    }

    protected void pause(String message, long timeout) {
        final ProgressDialog dialog = ProgressDialog.show(this, null, "Stopping ...", false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        FirebaseCrash.logcat(Log.INFO, TAG, "pause() dialog shown");
        Handler handler = new Handler();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, timeout);
        FirebaseCrash.logcat(Log.INFO, TAG, "pause() return");
    }

    protected void hideImageLogo() {
        View imageLogo = ButterKnife.findById(this, R.id.toolbar_logo);
        imageLogo.setVisibility(View.GONE);
    }

    protected void centerToolbar() {
        final View imageLogo = ButterKnife.findById(this, R.id.toolbar_logo);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        //width of action bar is the same as width of whole screen
        final int actionBarWidth = size.x;
        mToolbar.addOnLayoutChangeListener(
                new View.OnLayoutChangeListener() {

                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        float x = imageLogo.getX();
                        int logoImageWidth = imageLogo.getWidth();
                        int logoPosition = actionBarWidth / 2 - logoImageWidth / 2;
                        if (x != logoPosition) {
                            imageLogo.setX(logoPosition);
                            imageLogo.requestLayout();
                        } else {
                            mToolbar.removeOnLayoutChangeListener(this);
                        }
                    }
                }
        );
    }

    public BaseFragment getLastBaseFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = new ArrayList<>();
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment != null && fragment instanceof BaseFragment) {
                fragments.add(fragment);
            }
        }
        int size = fragments.size();
        return (BaseFragment) fragments.get(size - 1);
    }

    protected void setBurgerButtonState() {
    }

    protected void showBottomSheet(boolean show) {
    }

    protected void showFabButton(boolean show) {
    }
}
