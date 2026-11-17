package com.veloster.dashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** After boot: the floating window (if switched on) comes back without opening the dashboard first. */
public final class BootReceiver extends BroadcastReceiver {
    /** Brings the floating window back (if it is switched on) without opening the dashboard: after an update over adb while driving,
     *  `adb shell am broadcast -a com.veloster.dashboard.FLOAT_START`. Starts nothing that the setting has not already switched on. */
    static final String FLOAT_START="com.veloster.dashboard.FLOAT_START";
    public void onReceive(Context context,Intent intent){
        if(intent!=null&&(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())||FLOAT_START.equals(intent.getAction())))FloatService.startIfOn(context);
    }
}
