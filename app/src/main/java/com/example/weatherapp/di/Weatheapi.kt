package com.example.weatherapp.di

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface Weatherapi{
    @GET("/v1/current.json")
    suspend fun getWeather(
        @Query("key") apikey:String,
        @Query ("q") latlon : String
    ):Response<WeatherModel>


}

