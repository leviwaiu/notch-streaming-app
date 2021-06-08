//This class was a new addition to the app, written by Lilly for BSc Project
//Written with reference to: https://github.com/Gkxd/OSCTutorial/blob/master/app/src/main/java/com/dhua/osctutorial/MainActivity.java

package com.wearnotch.notchdemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import java.net.*;
import java.util.ArrayList;


public class oscService extends Service {

    private String myIP = BuildConfig.IPADDRESS;
    private String OSCIP = "/" + myIP;
    private int myPort = 7771;

    private OSCPortOut oscPortOut;

    private boolean isRunning;
    private Thread backgroundThread;

    private final IBinder binder = new LocalBinder();

    public oscService() {};

    public class LocalBinder extends Binder {
        oscService getService() {
            return oscService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        this.isRunning = false;
        this.backgroundThread = new Thread(oscTask);
    }

    private Runnable oscTask = new Runnable() {
        @Override
        public void run() {
            try {
                oscPortOut = new OSCPortOut(InetAddress.getByName(myIP), myPort);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            // ---------- INITIAL MSG ON STARTUP ---------------
            Log.i("OSC", "Sending initial message");

            ArrayList<Object> initialMsgList = new ArrayList<Object>();
            initialMsgList.add("0.0");
            initialMsgList.add("0.0");
            initialMsgList.add("0.0");
            initialMsgList.add("0.0");

            OSCMessage initialmsg = new OSCMessage(OSCIP, initialMsgList);

            try {
                oscPortOut.send(initialmsg);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    public void sendOSCMessage(String message) {
        ArrayList<Object> messages = new ArrayList<Object>();
        messages.add(message);
        OSCMessage oscmessage = new OSCMessage(OSCIP, messages);

        try {
            Log.i("OSC", "Sending OSC message...");
            oscPortOut.send(oscmessage);;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendOSCMessage(ArrayList<Object> list) {
        OSCMessage message = new OSCMessage(OSCIP, list);
        try {
            Log.i("OSC", "Sending OSC message...");
            oscPortOut.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        this.isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if( !this.isRunning) {
            this.isRunning = true;
            this.backgroundThread.start();
        }

        return START_STICKY;
    }
}

