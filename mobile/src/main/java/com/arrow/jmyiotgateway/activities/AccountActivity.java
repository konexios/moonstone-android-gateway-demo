package com.arrow.jmyiotgateway.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.fragments.AccountsListFragment;
import com.arrow.jmyiotgateway.fragments.BaseFragment;
import com.arrow.jmyiotgateway.fragments.NewAccountFragment;
import com.google.firebase.crash.FirebaseCrash;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.arrow.jmyiotgateway.Constant.CONFIG_EXTRA_INFO;

public class AccountActivity extends ActivityAbstract {
    public static final String IS_LAUNCHED = "is_launched";

    private final static String TAG = AccountActivity.class.getSimpleName();

    @BindView(R.id.account_fab_add)
    FloatingActionButton mAddButton;

    private Config mConfig;
    private AccountsListFragment mListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onCreate");
        Intent startIntent = getIntent();
        boolean isLaunched = startIntent.getBooleanExtra(IS_LAUNCHED, true);
        mConfig = new Config().loadActive(getApplicationContext());
        setContentView(R.layout.activity_account);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        hideImageLogo();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        BaseFragment fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!isLaunched) {
            mListFragment = new AccountsListFragment();
            fragment = mListFragment;
        } else {
            mTitle = getString(R.string.title_activity_account);
            getSupportActionBar().setTitle(mTitle);
            mAddButton.setVisibility(View.GONE);
            fragment = new NewAccountFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(CONFIG_EXTRA_INFO, Parcels.wrap(mConfig));
            bundle.putBoolean(IS_LAUNCHED, isLaunched);
            fragment.setArguments(bundle);
        }
        transaction.add(R.id.account_content_frame, fragment);
        transaction.commit();
    }

    @OnClick(R.id.account_fab_add)
    public void addAccount() {
        if (mListFragment != null) {
            mListFragment.addAccount();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setBurgerButtonState() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "setBurgerButtonState");
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            mAddButton.setVisibility(View.VISIBLE);
        } else {
            mAddButton.setVisibility(View.GONE);
        }
    }

    public interface AddAccountListener {
        void addAccount();
    }
}
