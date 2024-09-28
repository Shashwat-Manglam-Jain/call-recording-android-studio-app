package com.example.callrecordingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.example.callrecordingapp.RecordingService;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        Intent serviceIntent = new Intent(context, RecordingService.class);
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            serviceIntent.putExtra("state", "INCOMING");
            serviceIntent.putExtra("phoneNumber", phoneNumber);
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            serviceIntent.putExtra("state", "ANSWERED");
        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            serviceIntent.putExtra("state", "IDLE");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
