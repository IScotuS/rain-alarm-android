package com.example.rainalarm;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WeatherNotificationService extends Worker {
    private Context ctx;


    public WeatherNotificationService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.ctx = context;
    }


    @NonNull
    @Override
    public Result doWork() {
        WeatherDataService wds = new WeatherDataService(ctx);
        double longitude = getInputData().getDouble("longitude", wds.longitude);
        double latitude = getInputData().getDouble("latitude", wds.latitude);
        String city = getInputData().getString("city");

        wds.setLongitude(longitude);
        wds.setLatitude(latitude);

        wds.getWeatherReport(new WeatherDataService.WeatherForecastListener() {
            @Override
            public void onResponse(WeatherDataModel[] weather_forecast) {
                float [] elems = checkRain(weather_forecast);
                if (elems != null)
                    showNotification(elems[0], elems[1], city);
                Log.v("SCHEDULER", "Temp Success!");
            }

            @Override
            public void onError(String message) {
                Log.v("SCHEDULER", "Check Failure!");
//                Result.retry();
            }
        }, new WeatherDataService.WeatherImageListener() {
            @Override
            public void onResponse(WeatherDataModel weather_forecast_hour, int i) {
            }

            @Override
            public void onError(String message) {
//                Result.retry();
            }
        });

        return Result.success();
    }

    public void showNotification(float pop, float time, String city) {
        Intent activityIntent = new Intent(ctx, MainActivity.class);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(
                ctx,
                1,
                activityIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        @SuppressLint("DefaultLocale") NotificationCompat.Builder notification = new NotificationCompat.Builder(ctx, "rain_alarm")
                .setSmallIcon(R.drawable.ic_baseline_water_drop_24)
                .setContentTitle("Rain alert (" + city + ")")
                .setContentText(String.format("%.0f%% chance of rain at %.0f:00", pop*100, time))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(activityPendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);

        notificationManager.notify(1, notification.build());
    }



    private float[] checkRain(WeatherDataModel[] weather_forecast) {
        float time;
        for (int i = 1; i < 21; i++) {
            time = Float.parseFloat(weather_forecast[i].getTime().substring(0, 2));
            if (time >= 7 && time <= 22) {
                float pop = weather_forecast[i].getPop();
                if (pop > 0.3) {
                    return new float[]{pop, time};
                }
            }
        }
        return null;
    }
}
