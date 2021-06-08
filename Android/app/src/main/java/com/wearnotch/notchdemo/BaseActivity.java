//This class was pre-existing
//Added functionality to do with OSCService
package com.wearnotch.notchdemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.wearnotch.service.NotchAndroidService;
import com.wearnotch.service.network.NotchService;
import com.wearnotch.notchdemo.oscService;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all activities.
 */
public class BaseActivity extends AppCompatActivity implements ServiceConnection {

    private final List<NotchServiceConnection> mNotchServiceConnections =
            new ArrayList<NotchServiceConnection>();
    private oscServiceConnection oscServiceConnection;

    private boolean mServiceBound;
    private boolean mSOCServiceBound;
    private ComponentName mNotchServiceComponent;
    private ComponentName moscServiceComponent;
    protected NotchService mNotchService;
    protected oscService moscService;

    public void addNotchServiceConnection(NotchServiceConnection c) {
        if (!mServiceBound) {
            mServiceBound = true;
            Intent controlServiceIntent = new Intent(this, NotchAndroidService.class);
            bindService(controlServiceIntent, this, BIND_AUTO_CREATE);
        }

        if (!mNotchServiceConnections.contains(c)) {
            mNotchServiceConnections.add(c);
        }

        if (mNotchService != null) {
            c.onServiceConnected(mNotchService);
        }
    }

    public void addOSCServiceConnection(oscServiceConnection c) {
        Log.i("BaseActivity: ", "addOSCServiceConnection");
        if (!mSOCServiceBound) {
            mSOCServiceBound = true;
            Intent oscServiceIntent = new Intent(this, oscService.class);
            bindService(oscServiceIntent, this, BIND_AUTO_CREATE);
        }

        oscServiceConnection = c;

        if (moscService != null) {
            c.onServiceConnected(moscService);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        if (mServiceBound) {
            mServiceBound = false;
            unbindService(this);
        }

        if (mSOCServiceBound) {
            mSOCServiceBound = false;
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        boolean processed = false;
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                if (f instanceof BaseFragment && f.isVisible()) {
                    processed = ((BaseFragment) f).onBackPressed();
                }

                if (processed)
                    break;
            }
        }

        if (!processed) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i("BaseActivity: ", "OnServiceConnected with component: " + name.toString());
        if (service instanceof oscService.LocalBinder) {
            Log.i("BaseActivity: ", "OnServiceConnected with detected instance of oscService");
            moscServiceComponent = name;
            //Solved: Instead of service being the service, it's a "Local Binder" which needs to be cast and use getService to return the service
            oscService.LocalBinder binder = (oscService.LocalBinder) service;
            moscService = binder.getService();
            fireOSCServiceChange();

        }

        if (service instanceof NotchService) {
            mNotchServiceComponent = name;
            mNotchService = (NotchService) service;
            fireNotchServiceChange();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (name.equals(mNotchServiceComponent)) {
            mNotchServiceComponent = null;
            mNotchService = null;
            fireNotchServiceChange();
        }

        if (name.equals(moscServiceComponent)) {
            Log.i("BaseActivity: ", "OSC Service Disconnected");
            moscServiceComponent = null;
            moscService = null;
        }
    }

    private void fireNotchServiceChange() {
        for (NotchServiceConnection c : mNotchServiceConnections) {
            if (mNotchService != null) {
                c.onServiceConnected(mNotchService);
            } else {
                c.onServiceDisconnected();
            }
        }
    }

    private void fireOSCServiceChange() {
        if (moscService != null) {
            oscServiceConnection.onServiceConnected(moscService);
        } else {
            oscServiceConnection.onServiceDisconnected();
        }

    }

}
