package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arrow.acn.api.AcnApiService;
import com.arrow.acn.api.listeners.DeleteDeviceActionListener;
import com.arrow.acn.api.listeners.ListResultListener;
import com.arrow.acn.api.listeners.PostDeviceActionListener;
import com.arrow.acn.api.listeners.UpdateDeviceActionListener;
import com.arrow.acn.api.models.ActionParametersModel;
import com.arrow.acn.api.models.ApiError;
import com.arrow.acn.api.models.CommonResponse;
import com.arrow.acn.api.models.DeviceActionModel;
import com.arrow.acn.api.models.DeviceActionTypeModel;
import com.arrow.jmyiotgateway.AcnServiceHolder;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.activities.MainActivity;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.arrow.jmyiotgateway.fragments.ActionsFragment.ACTION_BUNDLE;
import static com.arrow.jmyiotgateway.fragments.ActionsFragment.DEVICE_HID_BUNDLE;

/**
 * Created by osminin on 8/8/2016.
 */

public final class NewActionFragment extends BaseFragment implements AdapterView.OnItemSelectedListener {
    private final static String TAG = NewActionFragment.class.getSimpleName();

    @BindView(R.id.new_action_name)
    TextView mName;
    @BindView(R.id.new_action_switch)
    SwitchCompat mSwitch;
    @BindView(R.id.new_action_description)
    TextView mDescription;
    @BindView(R.id.new_action_criteria)
    TextView mCriteria;
    @BindView(R.id.new_action_expiration)
    TextView mExpiration;
    @BindView(R.id.new_action_spinner)
    AppCompatSpinner mSpinner;

    @BindView(R.id.new_action_email_container)
    View mEmailContainer;
    @BindView(R.id.new_action_email)
    TextView mEmail;

    @BindView(R.id.new_action_skype_call_container)
    View mSkypeCallContainer;
    @BindView(R.id.new_action_message)
    TextView mMessage;
    @BindView(R.id.new_action_phone)
    TextView mPhone;
    @BindView(R.id.new_action_sip_address)
    TextView mSipAddress;

    @BindView(R.id.new_action_skype_meeting_container)
    View mSkypeMeetingContainer;
    @BindView(R.id.new_action_sip_address_meeting)
    TextView mSipAddressMeeting;

    @BindView(R.id.new_action_insight_alarm_container)
    View mInsightAlarmContainer;
    @BindView(R.id.new_action_severity)
    TextView mSeverity;
    @BindView(R.id.new_action_location)
    TextView mLocation;

    @BindView(R.id.new_action_url_container)
    View mUrlContainer;
    @BindView(R.id.new_action_url)
    TextView mUrl;

    @BindView(R.id.new_action_delete)
    Button mDelete;

    @BindString(R.string.action_type_email)
    String mEmailType;
    @BindString(R.string.action_type_skype_call)
    String mSkypeCallType;
    @BindString(R.string.action_type_skype_meeting)
    String mSkypeMeetingType;
    @BindString(R.string.action_type_insight_alarm)
    String mAlarmType;
    @BindString(R.string.action_type_url)
    String mUrlType;

    private AcnApiService mRestService;
    private String mDeviceHid;
    private List<DeviceActionTypeModel> mActionTypes;
    private DeviceActionModel mAction;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        mDeviceHid = bundle.getString(DEVICE_HID_BUNDLE);
        mRestService = AcnServiceHolder.getService();
        if (!bundle.containsKey(ACTION_BUNDLE)) {
            mRestService.getDeviceActionTypes(new ListResultListener<DeviceActionTypeModel>() {
                @Override
                public void onRequestSuccess(List<DeviceActionTypeModel> list) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "getActionTypes ok");
                    if (mRootView != null) {
                        mActionTypes = list;
                        List<CharSequence> names = new ArrayList<>(mActionTypes.size());
                        for (DeviceActionTypeModel model : mActionTypes) {
                            names.add(model.getName());
                        }
                        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(mContext,
                                R.layout.action_spinner_item, names);
                        mSpinner.setEnabled(true);
                        mSpinner.setAdapter(adapter);
                        mSpinner.setOnItemSelectedListener(NewActionFragment.this);
                    }
                }

                @Override
                public void onRequestError(ApiError error) {
                    FirebaseCrash.logcat(Log.ERROR, TAG, "getActionTypes error");
                    if (mRootView != null) {
                        showError(error);
                    }
                }
            });
            mName.setVisibility(View.GONE);
        } else {
            mAction = bundle.getParcelable(ACTION_BUNDLE);
            mDescription.setText(mAction.getDescription());
            mName.setText(mAction.getSystemName());
            mSwitch.setChecked(mAction.getEnabled());
            mCriteria.setText(mAction.getCriteria());
            mExpiration.setText(mAction.getExpiration().toString());
            mSpinner.setVisibility(View.GONE);
            mDelete.setVisibility(View.VISIBLE);
            showType(mAction);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.new_action_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_new_action, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        mSpinner.setEnabled(false);
        return mRootView;
    }

    @Override
    public String getTitle(Context context) {
        return mContext.getString(R.string.fragment_new_action);
    }

    @Override
    public void onDestroyView() {
        InputMethodManager imm = (InputMethodManager)
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mRootView.getWindowToken(), 0);
        super.onDestroyView();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = (String) parent.getSelectedItem();
        showType(selectedItem);
    }

    private void showType(String currentType) {
        mEmailContainer.setVisibility(View.GONE);
        mSkypeCallContainer.setVisibility(View.GONE);
        mSkypeMeetingContainer.setVisibility(View.GONE);
        mInsightAlarmContainer.setVisibility(View.GONE);
        mUrlContainer.setVisibility(View.GONE);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mDelete.getLayoutParams();
        int visibleContainerId = -1;
        if (mEmailType.equals(currentType)) {
            mEmailContainer.setVisibility(View.VISIBLE);
            visibleContainerId = mEmailContainer.getId();
        } else if (mSkypeCallType.equals(currentType)) {
            mSkypeCallContainer.setVisibility(View.VISIBLE);
            visibleContainerId = mSkypeCallContainer.getId();
        } else if (mSkypeMeetingType.equals(currentType)) {
            mSkypeMeetingContainer.setVisibility(View.VISIBLE);
            visibleContainerId = mSkypeMeetingContainer.getId();
        } else if (mAlarmType.equals(currentType)) {
            mInsightAlarmContainer.setVisibility(View.VISIBLE);
            visibleContainerId = mInsightAlarmContainer.getId();
        } else if (mUrlType.equals(currentType)) {
            mUrlContainer.setVisibility(View.VISIBLE);
            visibleContainerId = mUrlContainer.getId();
        }
        if (visibleContainerId != -1) {
            lp.addRule(RelativeLayout.BELOW, visibleContainerId);
        }
        mDelete.setLayoutParams(lp);
    }

    private void showType(DeviceActionModel action) {
        String currentType;
        if (!TextUtils.isEmpty(action.getParameters().getEmail())) {
            currentType = mEmailType;
            mEmail.setText(action.getParameters().getEmail());
        } else if (!TextUtils.isEmpty(action.getParameters().getMessage()) &&
                !TextUtils.isEmpty(action.getParameters().getSipAddress())) {
            currentType = mSkypeCallType;
            mMessage.setText(action.getParameters().getMessage());
            mPhone.setText(action.getParameters().getPhone());
            mSipAddress.setText(action.getParameters().getSipAddress());
        } else if (!TextUtils.isEmpty(action.getParameters().getSipAddress())) {
            currentType = mSkypeMeetingType;
            mSipAddress.setText(action.getParameters().getSipAddress());
        } else if (!TextUtils.isEmpty(action.getParameters().getSeverity())) {
            currentType = mAlarmType;
            mSeverity.setText(action.getParameters().getSeverity());
            mLocation.setText(action.getParameters().getLocation());
        } else {
            currentType = mUrlType;
            mUrl.setText(action.getParameters().getUrl());
        }
        showType(currentType);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_action_menu_accept:
                saveData();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.new_action_delete)
    public void delete() {
        mRestService.deleteDeviceAction(mDeviceHid, mAction.getIndex(), new DeleteDeviceActionListener() {
            @Override
            public void onDeviceActionDeleted(CommonResponse commonResponse) {
                if (mRootView != null) {
                    ((MainActivity) mContext).onBackPressed();
                }
            }

            @Override
            public void onDeviceActionDeleteFailed(ApiError apiError) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "postAction error");
                showError(apiError);
            }
        });
    }

    private void saveData() {
        Bundle bundle = getArguments();
        DeviceActionModel action = new DeviceActionModel();
        action.setEnabled(mSwitch.isChecked());

        if (!TextUtils.isEmpty(mCriteria.getText())) {
            action.setCriteria(mCriteria.getText().toString());
        }
        if (!TextUtils.isEmpty(mDescription.getText())) {
            action.setDescription(mDescription.getText().toString());
        }
        if (!TextUtils.isEmpty(mExpiration.getText())) {
            action.setExpiration(Integer.parseInt(mExpiration.getText().toString()));
        }

        ActionParametersModel parametersModel = new ActionParametersModel();
        String currentType = null;
        if (mEmail.getText() != null) {
            currentType = mEmailType;
            parametersModel.setEmail(mEmail.getText().toString());
        } else if (mMessage.getText() != null && mPhone.getText() != null && mSipAddress.getText() != null) {
            currentType = mSkypeCallType;
            parametersModel.setMessage(mMessage.getText().toString());
            parametersModel.setPhone(mPhone.getText().toString());
            parametersModel.setSipAddress(mSipAddress.getText().toString());
        } else if (mSipAddress.getText() != null) {
            currentType = mSkypeMeetingType;
            parametersModel.setSipAddress(mSipAddress.getText().toString());
        } else if (mSeverity.getText() != null && mLocation.getText() != null) {
            currentType = mAlarmType;
            parametersModel.setSeverity(mSeverity.getText().toString());
            parametersModel.setLocation(mLocation.getText().toString());
        } else if (mUrl.getText() != null) {
            currentType = mUrlType;
            parametersModel.setUrl(mUrl.getText().toString());
        }
        action.setSystemName(currentType.replace(" ", ""));
        action.setParameters(parametersModel);

        if (!bundle.containsKey(ACTION_BUNDLE)) {
            mRestService.postDeviceAction(mDeviceHid, action, new PostDeviceActionListener() {
                @Override
                public void postActionSucceed(CommonResponse commonResponse) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "postAction response");
                    if (mRootView != null) {
                        InputMethodManager imm = (InputMethodManager)
                                mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mRootView.getWindowToken(), 0);
                        ((MainActivity) mContext).onBackPressed();
                    }
                }

                @Override
                public void postActionFailed(ApiError error) {
                    FirebaseCrash.logcat(Log.ERROR, TAG, "postAction error");
                    showError(error);
                }
            });

        } else {
            DeviceActionModel oldAction = bundle.getParcelable(ACTION_BUNDLE);
            mRestService.updateDeviceAction(mDeviceHid, oldAction.getIndex(), action, new UpdateDeviceActionListener() {
                @Override
                public void onDeviceActionUpdated(CommonResponse commonResponse) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "updateAction response");
                    if (mRootView != null) {
                        ((MainActivity) mContext).onBackPressed();
                    }
                }

                @Override
                public void onDeviceActionUpdateFailed(ApiError error) {
                    FirebaseCrash.logcat(Log.ERROR, TAG, "updateAction error");
                    showError(error);
                }
            });
        }
    }
}
