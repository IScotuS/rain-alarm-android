package com.example.rainalarm;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class Util {
    // schedule the start of the service every 10 - 30 seconds
    public static void scheduleWork(Context context, double longitude, double latitude) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        Data.Builder data = new Data.Builder();
        data.putDouble("longitude", longitude);
        data.putDouble("latitude", latitude);

        PeriodicWorkRequest weatherRequest = new PeriodicWorkRequest.Builder(
                WeatherNotificationService.class, 3, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInputData(data.build())
                .addTag("rain_check")
                .build();


        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "checkWeather",
                ExistingPeriodicWorkPolicy.REPLACE,
                weatherRequest);
    }
}
