package com.example.rainalarm;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.POST_NOTIFICATIONS;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView txt_customMsg, txt_currentCity, txt_currentTemp;
    ImageView img_currentWeather;
    ImageButton btn_locateMe;
    TextView[] txt_tempHour = new TextView[21];
    TextView[] txt_timeHour = new TextView[21];
    TextView[] txt_popHour = new TextView[21];
    ImageView[] img_weatherHour = new ImageView[21];
    WeatherDataService wds = new WeatherDataService(this);
    ActivityResultLauncher<String> locationPermissionLauncher;
    ActivityResultLauncher<String> notificationPermissionLauncher;

    FusedLocationProviderClient fusedLocationClient;
    Geocoder geocoder;
    List<Address> addresses;

    boolean gotLocation = false;

    String city = "Paris";


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
        locationPermissionLauncher = registerForActivityResult(
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
                                                double latitude = location.getLatitude();
                                                double longitude = location.getLongitude();
                                                wds.setLatitude(latitude);
                                                wds.setLongitude(longitude);

                                                try {
                                                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                                    city = addresses.get(0).getLocality();
                                                    txt_currentCity.setText(city);
                                                    gotLocation = true;
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                updateWeather();
                                                Util.scheduleWork(MainActivity.this,
                                                        longitude,
                                                        latitude,
                                                        city);
                                            }
                                            else
                                                Toast.makeText(MainActivity.this, "Couldn't get current location", Toast.LENGTH_LONG).show();
                                        }

                                    });
                        }
                        else
                            Toast.makeText(MainActivity.this,
                                    "App needs location permission to run",
                                    Toast.LENGTH_LONG).show();
                    }
                });
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean isGranted) {
                        if (isGranted) {
                            // Use notifications
                            Toast.makeText(MainActivity.this,
                                    "Notifications permission granted",
                                    Toast.LENGTH_LONG).show();
                            Util.scheduleWork(MainActivity.this, wds.longitude, wds.latitude, city);

                        } else
                            Toast.makeText(MainActivity.this,
                                    "App needs notifications permission to send alerts",
                                    Toast.LENGTH_LONG).show();

                    }
                }
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    POST_NOTIFICATIONS) != PERMISSION_GRANTED)
                notificationPermissionLauncher.launch(POST_NOTIFICATIONS);
            else
                Util.scheduleWork(this, wds.longitude, wds.latitude, city);
        }
        else
            Util.scheduleWork(this, wds.longitude, wds.latitude, city);

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

        geocoder = new Geocoder(MainActivity.this, Locale.getDefault());


        btn_locateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for location permission
                if(ContextCompat.checkSelfPermission(MainActivity.this,
                        ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED)
                    locationPermissionLauncher.launch(ACCESS_COARSE_LOCATION);
                else
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
                            @NonNull
                            @Override
                            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                                return null;
                            }

                            @Override
                            public boolean isCancellationRequested() {
                                return false;
                            }
                        }).addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        // Logic to handle location object
                                        double latitude = location.getLatitude();
                                        double longitude = location.getLongitude();
                                        wds.setLatitude(latitude);
                                        wds.setLongitude(longitude);
                                        try {
                                            addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                            txt_currentCity.setText(addresses.get(0).getLocality());
                                            gotLocation = true;
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        updateWeather();
                                        Util.scheduleWork(MainActivity.this,
                                                longitude,
                                                latitude,
                                                addresses.get(0).getLocality());
                                    }
                                    else
                                        Toast.makeText(MainActivity.this, "Couldn't get current location", Toast.LENGTH_LONG).show();
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
                if (!gotLocation)
                    txt_currentCity.setText(city);
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