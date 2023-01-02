package com.example.rainalarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView txt_customMsg, txt_currentCity, txt_currentTemp;
    ImageView img_currentWeather;
    TextView[] txt_tempHour = new TextView[21];
    TextView[] txt_timeHour = new TextView[21];
    ImageView[] img_weatherHour = new ImageView[21];

    WeatherDataService wds = new WeatherDataService(this);

//    private BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Bundle bundle = intent.getExtras();
//            if (bundle)
//        }
//    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest weatherRequest = new PeriodicWorkRequest.Builder(
                WeatherNotificationService.class, 20, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag("rain_check")
                .build();

        WorkManager.getInstance(this).cancelAllWorkByTag("rain_check");
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "checkWeather",
                ExistingPeriodicWorkPolicy.KEEP,
                weatherRequest);

        txt_currentCity = findViewById(R.id.txt_currentCity);
        txt_currentTemp = findViewById(R.id.txt_currentTemp);
        txt_customMsg = findViewById(R.id.txt_customMsg);

        img_currentWeather = findViewById(R.id.img_currentWeather);
        // Fetch all views and store them in their respectful arrays
        for (int i = 0; i < 21; i++) {
            String txt_tempID = "txt_tempHour" + i;
            String txt_timeID = "txt_timeHour" + i;
            String img_weatherID = "img_weatherHour" + i;

            int tempID = getResources().getIdentifier(txt_tempID, "id", getPackageName());
            int timeID = getResources().getIdentifier(txt_timeID, "id", getPackageName());
            int weatherID = getResources().getIdentifier(img_weatherID, "id", getPackageName());

            txt_tempHour[i] = findViewById(tempID);
            txt_timeHour[i] = findViewById(timeID);
            img_weatherHour[i] = findViewById(weatherID);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        wds.getWeatherReport(new WeatherDataService.WeatherForecastListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(WeatherDataModel[] weather_forecast) {
                txt_currentCity.setText(weather_forecast[0].getCity());
                txt_currentTemp.setText(String.format(Locale.getDefault(), "%.0f°C", weather_forecast[0].getTemp()));
                txt_customMsg.setText(weather_forecast[0].getDescription().substring(0, 1).toUpperCase() + weather_forecast[0].getDescription().substring(1).toLowerCase());

                for (int i = 0; i < 21; i++) {
                    if (i == 0)
                        txt_timeHour[i].setText("Now");
                    else
                        txt_timeHour[i].setText(weather_forecast[i].getTime());
                    txt_tempHour[i].setText(String.format(Locale.getDefault(), "%.0f°", weather_forecast[i].getTemp()));
                }
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        }, new WeatherDataService.WeatherImageListener(){

            @Override
            public void onResponse(WeatherDataModel weather_forecast_hour, int i) {
                if (i == 0)
                    img_currentWeather.setImageBitmap(weather_forecast_hour.getWeather_icon());

                img_weatherHour[i].setImageBitmap(weather_forecast_hour.getWeather_icon());
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}