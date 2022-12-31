package com.example.rainalarm;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class WeatherDataService {
    final String API_KEY = "d1e397184bca47a4d90428c5fb8df78f";

    Context ctx;

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
    
    
    public void getWeatherReport(double latitude, double longitude, WeatherForecastListener weatherForecastListener, WeatherImageListener weatherImageListener) {
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
