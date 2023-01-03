package com.example.rainalarm;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView txt_customMsg, txt_currentCity, txt_currentTemp;
    ImageView img_currentWeather;
    ImageButton btn_locateMe;
    TextView[] txt_tempHour = new TextView[21];
    TextView[] txt_timeHour = new TextView[21];
    TextView[] txt_popHour = new TextView[21];
    ImageView[] img_weatherHour = new ImageView[21];
    WeatherDataService wds = new WeatherDataService(this);
    ActivityResultLauncher<String> permissionLauncher;
    FusedLocationProviderClient fusedLocationClient;

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

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        getWindow().getDecorView().setBackgroundColor(Color.rgb(73,121, 143));
        setContentView(R.layout.activity_main);



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onActivityResult(Boolean isGranted) {
                        if (isGranted) {
                            // Use location
                            Toast.makeText(MainActivity.this,
                                    "Location granted",
                                    Toast.LENGTH_LONG).show();
                            fusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                                        @Override
                                        public void onSuccess(Location location) {
                                            // Got last known location. In some rare situations this can be null.
                                            if (location != null) {
                                                // Logic to handle location object
                                                wds.setLatitude(location.getLatitude());
                                                wds.setLongitude(location.getLongitude());
                                                updateWeather();
                                                Util.scheduleWork(MainActivity.this,
                                                        location.getLongitude(),
                                                        location.getLatitude());
                                            }
                                        }
                                    });
                        }
                        else
                            Toast.makeText(MainActivity.this,
                                    "App needs location permission to run",
                                    Toast.LENGTH_LONG).show();
                    }
                });

        Util.scheduleWork(this, wds.longitude, wds.latitude);

        txt_currentCity = findViewById(R.id.txt_currentCity);
        txt_currentTemp = findViewById(R.id.txt_currentTemp);
        txt_customMsg = findViewById(R.id.txt_customMsg);
        btn_locateMe = findViewById(R.id.btn_locateMe);

//        img_currentWeather = findViewById(R.id.img_currentWeather);
        // Fetch all views and store them in their respectful arrays
        for (int i = 0; i < 21; i++) {
            String txt_tempID = "txt_tempHour" + i;
            String txt_timeID = "txt_timeHour" + i;
            String txt_popID = "txt_popHour" + i;
            String img_weatherID = "img_weatherHour" + i;

            int tempID = getResources().getIdentifier(txt_tempID, "id", getPackageName());
            int timeID = getResources().getIdentifier(txt_timeID, "id", getPackageName());
            int popID = getResources().getIdentifier(txt_popID, "id", getPackageName());
            int weatherID = getResources().getIdentifier(img_weatherID, "id", getPackageName());

            txt_tempHour[i] = findViewById(tempID);
            txt_timeHour[i] = findViewById(timeID);
            txt_popHour[i] = findViewById(popID);
            img_weatherHour[i] = findViewById(weatherID);
        }

        btn_locateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for location permission
                if(ContextCompat.checkSelfPermission(MainActivity.this,
                        ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED)
                    permissionLauncher.launch(ACCESS_COARSE_LOCATION);
                else
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        // Logic to handle location object
                                        wds.setLatitude(location.getLatitude());
                                        wds.setLongitude(location.getLongitude());
                                        updateWeather();
                                        Util.scheduleWork(MainActivity.this,
                                                location.getLongitude(),
                                                location.getLatitude());
                                    }
                                }
                            });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateWeather();
    }

    private void updateWeather() {
        Toast.makeText(MainActivity.this, "Updating..", Toast.LENGTH_SHORT).show();

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
                    txt_popHour[i].setText(String.format(Locale.getDefault(), "%.0f%%", weather_forecast[i].getPop()*100));
                }
                Toast.makeText(MainActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }, new WeatherDataService.WeatherImageListener(){

            @Override
            public void onResponse(WeatherDataModel weather_forecast_hour, int i) {
//                if (i == 0)
//                    img_currentWeather.setImageBitmap(weather_forecast_hour.getWeather_icon());

                img_weatherHour[i].setImageBitmap(weather_forecast_hour.getWeather_icon());
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }


}