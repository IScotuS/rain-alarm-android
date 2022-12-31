package com.example.rainalarm;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataService extends JobService {
    final String API_KEY = "d1e397184bca47a4d90428c5fb8df78f";

    final double latitude = 48.3804453;
    final double longitude = -4.5120015;

    Context ctx;

    public WeatherDataService() {
//        super("WeatherDataService");
    }

    public WeatherDataService(Context ctx) {
        this.ctx = ctx;
    }

    public interface WeatherForecastListener {
        void onResponse(WeatherDataModel[] weather_forecast);

        void onError(String message);
    }

    public interface WeatherImageListener {
        void onResponse(WeatherDataModel weather_forecast_hour, int i);

        void onError(String message);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Intent intent = new Intent("com.example.rainalarm");

        getWeatherReport(new WeatherForecastListener() {
            @Override
            public void onResponse(WeatherDataModel[] weather_forecast) {
                intent.putExtra("temp", weather_forecast);
                Log.v("SCHEDULER", "Temp Success!");
                Toast.makeText(getApplicationContext(), "Temp Success!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                intent.putExtra("error_temp", message);
            }
        }, new WeatherImageListener() {
            @Override
            public void onResponse(WeatherDataModel weather_forecast_hour, int i) {
                intent.putExtra("icon", weather_forecast_hour);
                intent.putExtra("index", i);
            }

            @Override
            public void onError(String message) {
                intent.putExtra("error_icon", message);
            }
        });

        Util.scheduleJob(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    public void getWeatherReport(WeatherForecastListener weatherForecastListener, WeatherImageListener weatherImageListener) {
        String url = "https://pro.openweathermap.org/data/2.5/forecast/hourly?lat=" + latitude
                + "&lon=" + longitude + "&appid=" + API_KEY + "&units=metric&cnt=21";

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray weather_list = response.getJSONArray("list");
                            WeatherDataModel[] weather_forecast = new WeatherDataModel[21];

                            for (int i = 0; i < 21; i++) {
                                weather_forecast[i] = new WeatherDataModel();

                                weather_forecast[i].setCity(
                                        response.getJSONObject("city").optString("name"));
                                weather_forecast[i].setTime(
                                        weather_list.getJSONObject(i).getString("dt_txt")
                                                .substring(11, 16));
                                weather_forecast[i].setTemp(
                                        (float) weather_list.getJSONObject(i).getJSONObject("main")
                                                .optDouble("temp"));
                                weather_forecast[i].setDescription(
                                        weather_list.getJSONObject(i).getJSONArray("weather")
                                                .getJSONObject(0).getString("description"));
                                // Get Weather Icon from API
                                loadImage(weather_list.getJSONObject(i).getJSONArray("weather")
                                        .getJSONObject(0).getString("icon"), weather_forecast[i], i, weatherImageListener);

                                weather_forecast[i].setFeels_like(
                                        (float) weather_list.getJSONObject(i).getJSONObject("main")
                                                .optDouble("feels_like"));

                                if (weather_list.getJSONObject(i).has("rain")) {
                                    weather_forecast[i].setPrecipitation(
                                            (float) weather_list.getJSONObject(i).getJSONObject("rain")
                                                    .getDouble("1h"));
                                }

                                weather_forecast[i].setPop(
                                        (float) weather_list.getJSONObject(i).optDouble("pop"));
                            }

                            weatherForecastListener.onResponse(weather_forecast);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                weatherForecastListener.onError("Something went wrong..");
            }
        });
        MySingleton.getInstance(ctx).addToRequestQueue(jsonRequest);
    }

    public void loadImage(String iconCode, WeatherDataModel weather_forecast_hour, int index, WeatherImageListener weatherImageListener) {
        String url = "https://openweathermap.org/img/wn/" + iconCode + ".png";
        ImageRequest imageRequest =
                new ImageRequest(url, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        weather_forecast_hour.setWeather_icon(response);
                        weatherImageListener.onResponse(weather_forecast_hour, index);
                    }
                }, 0, 0, null, null, error -> weatherImageListener.onError("Couldn't load image"));
        MySingleton.getInstance(ctx).addToRequestQueue(imageRequest);
    }
}
