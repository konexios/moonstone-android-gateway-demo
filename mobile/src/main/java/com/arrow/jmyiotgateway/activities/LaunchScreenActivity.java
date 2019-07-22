package com.arrow.jmyiotgateway.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.arrow.acn.api.models.ApiError;
import com.arrow.jmyiotgateway.BuildConfig;
import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.miramonti.acn.EventAcnApiService;
import com.arrow.jmyiotgateway.miramonti.acn.EventSimpleResponseListener;
import com.arrow.jmyiotgateway.miramonti.acn.eventServiceHolders.EventAcnServiceHolder;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.SocialEventListResponse;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.SocialEventResponse;
import com.google.firebase.crash.FirebaseCrash;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static android.util.Log.VERBOSE;
import static com.arrow.jmyiotgateway.Constant.DEV_ENVIRONMENT;

/**
 * Created by batrakov on 12.01.18.
 */

public class LaunchScreenActivity extends AppCompatActivity {

    private static int sActiveEventsCounter = 0;
    private EventAcnApiService mEventAcnApiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sActiveEventsCounter = 0;
        setContentView(R.layout.event_launch_screen_in_layout);
        DelayAsyncTask delayAsyncTask = new DelayAsyncTask(this);
        delayAsyncTask.execute();
    }

    private static class DelayAsyncTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = DelayAsyncTask.class.getName();
        WeakReference<LaunchScreenActivity> mLaunchScreenActivityWeakReference;



        DelayAsyncTask(LaunchScreenActivity aLaunchScreenActivity) {
            mLaunchScreenActivityWeakReference = new WeakReference<>(aLaunchScreenActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... aVoids) {
            final LaunchScreenActivity launchScreenActivity = mLaunchScreenActivityWeakReference.get();
            launchScreenActivity.createAcnApiService();

            launchScreenActivity.mEventAcnApiService.findSocialEvents(new EventSimpleResponseListener<SocialEventListResponse>() {
                @Override
                public void onRequestSuccess(SocialEventListResponse response) {
                    FirebaseCrash.logcat(VERBOSE, TAG, "onSuccess");
                    Date currentDate = Calendar.getInstance().getTime();
                    Set<String> activeEvents = new ArraySet<>();
                    for (SocialEventResponse socialEventResponse :
                            response.getData()) {
                        Date startEventDate = convertStringToDate(socialEventResponse.getStartDate());
                        Date endEventDate = convertStringToDate(socialEventResponse.getEndDate());
                        if (currentDate.after(startEventDate) && currentDate.before(endEventDate)) {
                            sActiveEventsCounter++;
                            activeEvents.add(socialEventResponse.getName());
                        }
                    }

                    SharedPreferences preferences = launchScreenActivity.getSharedPreferences(Constant.SHARED_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putStringSet(Constant.ACTIVE_EVENTS, activeEvents);
                    editor.apply();
                }

                @Override
                public void onRequestError(ApiError error) {
                    FirebaseCrash.logcat(VERBOSE,  TAG, "onFailure");
                }
            });

            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException aE) {
                aE.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            LaunchScreenActivity launchScreenActivity = mLaunchScreenActivityWeakReference.get();
            ArrayList<Config> configList = (ArrayList<Config>) Config.loadAll(launchScreenActivity);
            if (launchScreenActivity != null) {
                if (0 == configList.size() && sActiveEventsCounter > 0) {
                    Intent intent = new Intent(launchScreenActivity, EventRegistrationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    launchScreenActivity.startActivityForResult(intent, 0);
                    launchScreenActivity.overridePendingTransition(0, 0);
                } else {
                    Intent intent = new Intent(launchScreenActivity, MainActivity.class);
                    launchScreenActivity.startActivityForResult(intent, 0);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    private void createAcnApiService() {
        Config config = new Config();
        if (DEV_ENVIRONMENT) {
            config.setServerEnvironment("Development");
        } else {
            config.setServerEnvironment("Production");
        }
        mEventAcnApiService = EventAcnServiceHolder.createService(this, config);
    }

    private static Date convertStringToDate(String aStringDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Date date = null;
        try {
            date = format.parse(aStringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
