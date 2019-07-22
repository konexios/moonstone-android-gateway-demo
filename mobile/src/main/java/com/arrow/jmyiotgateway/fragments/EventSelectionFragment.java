package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by batrakov on 12.01.18.
 */

public class EventSelectionFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.event_selection_spinner)
    Spinner mEventSelection;

    @BindView(R.id.confirm_event_choice)
    Button mConfirmButton;

    @BindView(R.id.back_to_event_confirmation_button)
    Button mBackButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_event_selection, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        mConfirmButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        final Set<String> spinnerElementsSet = sharedPreferences.getStringSet(
                Constant.ACTIVE_EVENTS, null);
        if (spinnerElementsSet != null) {
            String[] events = spinnerElementsSet.toArray(new String[spinnerElementsSet.size()]);
            List<String> sortedEvents = Arrays.asList(events);
            Collections.sort(sortedEvents);
            ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(
                    mContext,
                    android.R.layout.simple_spinner_dropdown_item,
                    sortedEvents);
            mEventSelection.setAdapter(stringArrayAdapter);
            mEventSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(Constant.Preference.KEY_SELECTED_EVENT, events[position]);
                    editor.apply();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(Constant.Preference.KEY_SELECTED_EVENT, events[0]);
                    editor.apply();
                }
            });
        }
    }

    @Override
    public String getTitle(Context context) {
        return "";
    }

    @Override
    public void onClick(View aView) {
        BaseFragment fragment = null;
        android.support.v4.app.FragmentManager fragmentManager = ((AppCompatActivity) mContext).
                getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        switch (aView.getId()) {
            case R.id.confirm_event_choice:
                fragment = new EventRegistrationFragment();
                break;
            case R.id.back_to_event_confirmation_button:
                ((AppCompatActivity) mContext).onBackPressed();
                break;
        }

        if (fragment != null) {
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.event_frame, fragment);
            transaction.addToBackStack(this.getTitle(mContext));
            transaction.commit();
        }
    }
}
