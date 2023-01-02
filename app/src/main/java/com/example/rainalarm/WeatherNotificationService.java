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

        Intent intent = new Intent("com.example.rainalarm");
        wds.getWeatherReport(new WeatherDataService.WeatherForecastListener() {
            @Override
            public void onResponse(WeatherDataModel[] weather_forecast) {
                float [] elems = checkRain(weather_forecast);
                if (elems != null)
                    showNotification(elems[0], elems[1]);
                intent.putExtra("temp", weather_forecast);
                Log.v("SCHEDULER", "Temp Success!");
                Toast.makeText(getApplicationContext(), "Temp Success!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                intent.putExtra("error_temp", message);
                Log.v("SCHEDULER", "Temp Failure!");
//                Result.retry();
            }
        }, new WeatherDataService.WeatherImageListener() {
            @Override
            public void onResponse(WeatherDataModel weather_forecast_hour, int i) {
                intent.putExtra("icon", weather_forecast_hour);
                intent.putExtra("index", i);
            }

            @Override
            public void onError(String message) {
                intent.putExtra("error_icon", message);
//                Result.retry();
            }
        });

        return Result.success();
    }

    public void showNotification(float pop, float time) {
        Intent activityIntent = new Intent(ctx, MainActivity.class);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(
                ctx,
                1,
                activityIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        @SuppressLint("DefaultLocale") NotificationCompat.Builder notification = new NotificationCompat.Builder(ctx, "rain_alarm")
                .setSmallIcon(R.drawable.ic_baseline_water_drop_24)
                .setContentTitle("Rain alert")
                .setContentText(String.format("%.0f%% chance of rain at %.0f:00", pop*100, time))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(activityPendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);

        notificationManager.notify(1, notification.build());
    }



    private float[] checkRain(WeatherDataModel[] weather_forecast) {
        float time;
        int flag = 0;
        for (int i = 0; i < 21; i++) {
            time = Float.parseFloat(weather_forecast[i].getTime().substring(0, 2));
            if (time > 7 && time < 22) {
                flag = 1;
                float pop = weather_forecast[i].getPop();
                if (pop > 0.5) {
                    return new float[]{pop, time};
                }
            }
            else if (flag == 1)
                break;
        }
        return null;
    }
}
