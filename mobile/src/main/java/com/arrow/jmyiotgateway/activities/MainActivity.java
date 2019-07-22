package com.arrow.jmyiotgateway.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.cloud.CloudService;
import com.arrow.jmyiotgateway.fragments.BaseFragment;
import com.arrow.jmyiotgateway.fragments.DeviceListFragment;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.crash.FirebaseCrash;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.arrow.jmyiotgateway.Constant.CONFIG_EXTRA_INFO;

public class MainActivity extends ActivityAbstract implements
        NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static int ACCOUNT_ACTIVITY_REQUEST_CODE = 100;
    private final static String LAST_USED_SP = "lastUsedTime";

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.drawer_menu_view)
    NavigationView mNavigationView;
    @BindView(R.id.main_fab_add)
    FloatingActionButton mActionButton;
    @BindView(R.id.details_bottom_sheet)
    View mBottomSheet;

    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private Config mConfig;
    private DeviceListFragment mMainFragment;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseCrash.logcat(Log.INFO, TAG, "onCreate() ...");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_menu_open,
                R.string.drawer_menu_close);


        mActionBarDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.onBackPressed();
            }
        });
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
        mNavigationView.inflateMenu(R.menu.drawer_menu);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setItemIconTintList(null);

        setBurgerButtonState();

        mConfig = new Config().loadActive(getApplicationContext());
        if (savedInstanceState == null) {
            addDeviceListFragment();
            FirebaseCrash.logcat(Log.INFO, TAG, "onCreate() registerGateway ...");
        }
        setDrawerTitle();

        if (TextUtils.isEmpty(mConfig.getUserId())) {
            Intent intent = new Intent(this, AccountActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            initialize();
        }
        centerToolbar();
        mBottomSheet.setVisibility(View.GONE);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        long lastUsed = sp.getLong(LAST_USED_SP, 0);
        TextView lastUsedTv = ButterKnife.findById(mNavigationView.getHeaderView(0), R.id.nav_header_date);
        if (lastUsed != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d");
            lastUsedTv.setText(sdf.format(new Date(lastUsed)));
        } else {
            lastUsedTv.setVisibility(View.INVISIBLE);
        }
    }

    private void addDeviceListFragment() {
        mMainFragment = new DeviceListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CONFIG_EXTRA_INFO, Parcels.wrap(mConfig));
        mMainFragment.setArguments(bundle);
        showHomeScreen(mMainFragment);
    }

    private void showHomeScreen(BaseFragment fragment) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "showHomeScreen");
        getSupportFragmentManager().popBackStack();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.content_frame, fragment);
        transaction.commit();
        setDrawerTitle();
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onNavigationItemSelected: " + item.getTitle());
        switch (item.getItemId()) {
            case R.id.drawer_menu_item_account:
                Intent intent = new Intent(this, AccountActivity.class);
                intent.putExtra(AccountActivity.IS_LAUNCHED, false);
                startActivityForResult(intent, ACCOUNT_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.drawer_menu_item_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.drawer_menu_item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        getSupportFragmentManager().popBackStackImmediate();
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ACCOUNT_ACTIVITY_REQUEST_CODE) {
            mConfig = Parcels.unwrap(data.getParcelableExtra(CONFIG_EXTRA_INFO));
            mTitle = getString(R.string.app_name) + " - " + mConfig.getProfileName();
            setDrawerTitle();
            if (mMainFragment != null) {
                mMainFragment.unbindServices();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(mMainFragment);
                transaction.commit();
                mMainFragment = null;
                addDeviceListFragment();
            }

            Intent cloudServiceIntent = new Intent(getBaseContext(), CloudService.class);
            stopService(cloudServiceIntent);
            initialize();
            //clear shared preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.commit();
        }
    }

    @OnClick(R.id.main_fab_add)
    public void onAddNewDevice() {
        BaseFragment lastFragment = getLastBaseFragment();
        if (lastFragment instanceof AddFabListener) {
            ((AddFabListener) lastFragment).addFabClicked();
        }
    }

    @Override
    protected void setBurgerButtonState() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "setBurgerButtonState");
        int count = getSupportFragmentManager().getBackStackEntryCount();
        mActionBarDrawerToggle.setDrawerIndicatorEnabled(count == 0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(count > 0);
        mActionBarDrawerToggle.syncState();
        if (count == 0) {
            mToolbar.setNavigationIcon(R.drawable.ic_more_vert_white_24dp);
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_24dp);
        }
    }

    @Override
    protected void showFabButton(boolean show) {
        mActionButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void showBottomSheet(boolean show) {
        mBottomSheet.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void initialize() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "initialize");
        //start background service for sending data to the cloud
        Intent cloudServiceIntent = new Intent(getBaseContext(), CloudService.class);
        cloudServiceIntent.putExtra(CONFIG_EXTRA_INFO, Parcels.wrap(mConfig));
        startService(cloudServiceIntent);

        // force to register/login on start up if needed
        if (TextUtils.isEmpty(mConfig.getUserId())) {
            startActivity(new Intent(this, AccountActivity.class));
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mToolbar.setNavigationIcon(R.drawable.ic_more_vert_white_24dp);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient.connect();
        AppIndex.AppIndexApi.start(mClient, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mClient, getIndexApiAction());
        mClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(LAST_USED_SP, System.currentTimeMillis());
        editor.commit();
        super.onDestroy();
    }

    private void setDrawerTitle() {
        TextView menuTitle = ButterKnife.findById(mNavigationView.getHeaderView(0), R.id.nav_header_profile_name);
        menuTitle.setText(mConfig.getProfileName());
    }

    public interface AddFabListener {
        void addFabClicked();
    }
}
