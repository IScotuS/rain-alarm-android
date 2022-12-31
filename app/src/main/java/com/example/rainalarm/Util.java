package com.example.rainalarm;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

public class Util {
    // schedule the start of the service every 10 - 30 seconds
    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, WeatherDataService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(10000); // wait at least
        builder.setOverrideDeadline(30000); // maximum delay

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

        JobScheduler jobScheduler;
        jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());


    }
}
