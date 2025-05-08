package com.mi3mien.weatherapp.model

import com.mi3mien.weatherapp.R

data class WeatherModel(
    val temperature: String,
    val highTemp: String,
    val lowTemp: String,
    val city: String,
    val icon: Int
)

fun getDummyWeatherData(): List<WeatherModel> {
    return listOf(
        WeatherModel("27° C", "29", "24", "Ha Noi, Bangkok", R.drawable.clouds),
        WeatherModel("28° C", "30", "25", "Ha Noi, Bangkok", R.drawable.rain),
        WeatherModel("27° C", "29", "24", "Ha Noi, Bangkok", R.drawable.sunny),
        WeatherModel("26° C", "28", "23", "Ha Noi, Bangkok", R.drawable.storm)
    )
}