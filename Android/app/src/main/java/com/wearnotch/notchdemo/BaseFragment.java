//This class was pre-existing
//Added functionality to do with OSCService
package com.wearnotch.notchdemo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.wearnotch.service.network.NotchService;

/**
 * Base class for fragments.
 */
public class BaseFragment extends Fragment implements NotchServiceConnection, oscServiceConnection {

    protected Context mApplicationContext;
    protected NotchService mNotchService;
    protected oscService moscService;

    protected void bindNotchService() {
        BaseActivity activity = (BaseActivity) getActivity();
        if (activity != null) {
            activity.addNotchServiceConnection(this);
        }
    }

    protected void bindOscService() {
        BaseActivity activity = (BaseActivity) getActivity();
        if (activity != null) {
            activity.addOSCServiceConnection(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplicationContext = getActivity().getApplicationContext();
    }

    public boolean onBackPressed() {
        return false;
    }

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    public void setActionBarTitle(int stringResId) {
        BaseActivity activity = getBaseActivity();
        if (activity != null) {
            activity.getSupportActionBar().setTitle(stringResId);
        }
    }

    /**
     * Override point for subclasses.
     */
    @Override
    public void onServiceConnected(NotchService notchService) {
        Log.i("BaseFragment: ", "onServiceConnected NotchService: " + notchService.toString());
            mNotchService = notchService;
    }

    @Override
    public void onServiceConnected(oscService oscService) {
        //never getting to here?
        Log.i("BaseFragment: ", "onServiceConnected OSCService");

        moscService = oscService;
    }

    /**
     * Override point for subclasses.
     */
    @Override
    public void onServiceDisconnected() {
        mNotchService = null;
        moscService = null;
    }

    protected void fireInvalidateOptionsMenu() {
        if (isAdded()) {
            getActivity().runOnUiThread(mInvalidateOptionsMenu);
        }
    }

    private Runnable mInvalidateOptionsMenu = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) {
                getActivity().invalidateOptionsMenu();
            }
        }
    };

}
