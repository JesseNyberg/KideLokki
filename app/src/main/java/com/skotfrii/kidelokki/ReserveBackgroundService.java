package com.skotfrii.kidelokki;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Locale;

public class ReserveBackgroundService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ReserveChannel";

    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;

    private CountDownTimer countDownTimer;

    private MainActivity mainActivity;

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    // Method to handle countdown finish
    private void handleCountdownFinish() {
        // Trigger the reservation process in the MainActivity when the countdown finishes
        if (mainActivity != null) {
            mainActivity.handleProductFetch();
        }
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        ReserveBackgroundService getService() {
            return ReserveBackgroundService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPrefs = getSharedPreferences("ProductPrefs", MODE_PRIVATE);

        // Create and show the notification to run the service in the foreground
        createNotificationChannel();
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

    }

    public void startReservation(long timeRemaining) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        sharedPrefs.edit().putBoolean("isCountdownActive", true).apply();

        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Intent intent = new Intent("com.skotfrii.kidelokki.COUNTDOWN_UPDATED");
                intent.putExtra("countdown", millisUntilFinished / 1000);
                LocalBroadcastManager.getInstance(ReserveBackgroundService.this).sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                // Handle the countdown finish in the MainActivity
                Intent intent = new Intent("com.skotfrii.kidelokki.COUNTDOWN_FINISHED");
                LocalBroadcastManager.getInstance(ReserveBackgroundService.this).sendBroadcast(intent);
                handleCountdownFinish();
            }
        }.start();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPrefs.edit().putBoolean("isServiceRunning", true).apply();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPrefs.edit().putBoolean("isServiceRunning", false).apply();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Reserve Background Service Channel",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Reserve Background Service")
                .setContentText("Running...")
                //.setSmallIcon(R.drawable.seagull)
                .setPriority(NotificationCompat.PRIORITY_MIN) // Set the priority to minimize notification visibility
                .setContentIntent(pendingIntent)
                .build();
    }

    public void cancelCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;


            sharedPrefs.edit().putBoolean("isServiceRunning", false).apply();
            stopForeground(true);
            stopSelf();
        }
    }
}
