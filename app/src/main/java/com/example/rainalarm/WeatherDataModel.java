package com.example.rainalarm;

import android.graphics.Bitmap;

public class WeatherDataModel {

    private String city;
    private float temp;
    private float feels_like;
    private String time;
    private String description;
    private float precipitation;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //Possibility of precipitation
    private float pop;

    public Bitmap getWeather_icon() {
        return weather_icon;
    }

    public void setWeather_icon(Bitmap weather_icon) {
        this.weather_icon = weather_icon;
    }

    private Bitmap weather_icon;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public float getFeels_like() {
        return feels_like;
    }

    public void setFeels_like(float feels_like) {
        this.feels_like = feels_like;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(float precipitation) {
        this.precipitation = precipitation;
    }

    public float getPop() {
        return pop;
    }

    public void setPop(float pop) {
        this.pop = pop;
    }
}
