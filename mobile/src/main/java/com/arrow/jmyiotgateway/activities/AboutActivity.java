package com.arrow.jmyiotgateway.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.arrow.acn.api.BuildConfig;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.Util;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutActivity extends ActivityAbstract {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.textView_version)
    TextView mVersionView;

    @BindString(R.string.activity_about_version)
    String mVersionText;
    @BindString(R.string.activity_about_version_sdk)
    String mSdkVersionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        mVersionView.setText(String.format("%s %s \n %s %s", mVersionText, Util.getVersionNumber(),
                mSdkVersionText, BuildConfig.VERSION_NAME));

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        hideImageLogo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
