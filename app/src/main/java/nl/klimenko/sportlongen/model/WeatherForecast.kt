package nl.klimenko.sportlongen.model

import retrofit2.http.Field
import java.io.Serializable
/*
Model for OpenWeatherMap API response
 */
data class WeatherForecast(
    var lat: Double?,
    var lon: Double?,
    var timezone: String?,
    var current: Current?
) : Serializable

data class Current(
    val dt: Long?,
    val sunrise: Long?,
    val sunset: Long?,
    val temp: Double?,
    val feels_like: Double?,
    val pressure: Int?,
    val humidity: Int?,
    val dew_point: Double?,
    val uvi: Double?,
    val clouds: Int?,
    val visibility: Long?,
    val wind_speed: Double?,
    val wind_deg: Int?,
    val weather: List<Weather>?,
    val rain: Rain?
) : Serializable

data class Rain(
    @Field("1h")
    val h: Double
) : Serializable

data class Weather(
    val id: Int?,
    val main: String?,
    val description: String?,
    val icon: String
) : Serializable