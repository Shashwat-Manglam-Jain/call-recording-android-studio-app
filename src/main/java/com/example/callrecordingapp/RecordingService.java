package com.example.callrecordingapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class RecordingService extends Service {
    private MediaRecorder recorder;
    private boolean isRecording = false;
    private static final String CHANNEL_ID = "CallRecordingServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String state = intent.getStringExtra("state");
        String phoneNumber = intent.getStringExtra("phoneNumber");

        if ("INCOMING".equals(state) || "ANSWERED".equals(state)) {
            startForeground(1, getNotification());
            startRecording(phoneNumber);
        } else if ("IDLE".equals(state)) {
            stopRecording();
            stopForeground(true);
        }

        return START_NOT_STICKY;
    }

    private void startRecording(String phoneNumber) {
        if (isRecording) return;

        File outputFile = new File(getExternalFilesDir(null), "CallRecording_" + phoneNumber + ".mp3");
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(outputFile.getAbsolutePath());

        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
        } catch (IOException e) {
            Log.e("RecordingService", "Failed to start recording", e);
        }
    }

    private void stopRecording() {
        if (!isRecording) return;

        recorder.stop();
        recorder.release();
        recorder = null;
        isRecording = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification getNotification() {
        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Recording Call")
                    .setContentText("Recording in progress")
                    .setSmallIcon(R.drawable.ic_notification);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Call Recording Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
