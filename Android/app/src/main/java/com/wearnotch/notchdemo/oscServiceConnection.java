//This class was a new addition to the app, written by Lilly
package com.wearnotch.notchdemo;

import com.wearnotch.notchdemo.oscService;

public interface oscServiceConnection {
    void onServiceConnected(oscService oscService);
    void onServiceDisconnected();
}

