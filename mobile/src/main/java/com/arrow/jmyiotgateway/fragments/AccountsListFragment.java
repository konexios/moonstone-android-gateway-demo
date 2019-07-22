package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.activities.AccountActivity;
import com.google.firebase.crash.FirebaseCrash;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arrow.jmyiotgateway.Constant.CONFIG_EXTRA_INFO;

/**
 * Created by osminin on 10/31/2016.
 */

public final class AccountsListFragment extends BaseFragment implements AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, AccountActivity.AddAccountListener {
    public final static String TAG = AccountsListFragment.class.getSimpleName();

    @BindView(R.id.fragment_accounts_list)
    ListView mListView;
    @BindView(R.id.fragment_accounts_swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private List<Config> mConfigsList;
    private ArrayAdapter<Config> mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onCreateView");
        mRootView = inflater.inflate(R.layout.fragment_accounts_list, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        update(Config.loadAll(mContext));
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_fragment_accounts_list);
    }

    private void showNewAccountFragment(Config config) {
        BaseFragment fragment = new NewAccountFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CONFIG_EXTRA_INFO, Parcels.wrap(config));
        fragment.setArguments(bundle);
        FragmentTransaction transaction = ((AppCompatActivity) mContext).
                getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.account_content_frame, fragment);
        transaction.addToBackStack(fragment.getFragmentTag());
        transaction.commit();
    }

    private void update(List<Config> list) {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "update");
        mConfigsList = list;
        mAdapter = new AccountsAdapter(mContext, R.layout.layout_account_item, mConfigsList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Config config = mConfigsList.get(position);
        showNewAccountFragment(config);
    }

    @Override
    public void onRefresh() {
        update(Config.loadAll(mContext));
    }

    @Override
    public void addAccount() {
        showNewAccountFragment(new Config());
    }

    private class AccountsAdapter extends ArrayAdapter<Config> {

        public AccountsAdapter(Context context, int resource, List<Config> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.layout_account_item, null);
            }

            Config accountConfig = getItem(position);

            if (accountConfig != null) {
                TextView accountTitle = ButterKnife.findById(v, R.id.account_name);
                View activeLabel = ButterKnife.findById(v, R.id.account_active);
                if (accountTitle != null) {
                    accountTitle.setText(accountConfig.getProfileName());
                }
                if (activeLabel != null) {
                    int visibility = accountConfig.getActive() ? View.VISIBLE : View.INVISIBLE;
                    activeLabel.setVisibility(visibility);
                }
            }

            return v;
        }
    }
}
