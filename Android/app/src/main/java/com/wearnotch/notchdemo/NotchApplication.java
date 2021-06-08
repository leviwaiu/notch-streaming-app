package com.wearnotch.notchdemo;

import android.app.Application;
import android.content.Intent;

public class NotchApplication extends Application {

    private static NotchApplication mInst;

    public static NotchApplication getInst() {
        return mInst;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInst = this;

//        Intent background = new Intent(this, socketService.class);
//        startService(background);

        //Intent background = new Intent(this, httpService.class);
        //startService(background);

        //Intent osc = new Intent(this, oscService.class);
        //startService(osc);

    }
}
