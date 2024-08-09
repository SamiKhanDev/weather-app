package com.example.weatherapp.di

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val baseurl= "https://api.weatherapi.com"

    private fun getInstance():Retrofit{
        return Retrofit.Builder()
            .baseUrl(baseurl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val weatherapi: Weatherapi= getInstance().create(Weatherapi::class.java)
}