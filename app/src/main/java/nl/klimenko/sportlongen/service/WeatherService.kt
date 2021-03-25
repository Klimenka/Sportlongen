package nl.klimenko.sportlongen.service

import nl.klimenko.sportlongen.model.AirQuality
import nl.klimenko.sportlongen.model.WeatherForecast
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/onecall")
    fun getWeather(
        @Query("lat") lat: Double?,
        @Query("lon") lon: Double?,
        @Query("units") units: String?,
        @Query("appid") appId: String
    ): Call<WeatherForecast>

    @GET("data/2.5/air_pollution")
    fun getAirQuality(
        @Query("lat") lat: Double?,
        @Query("lon") lon: Double?,
        @Query("appid") appId: String
    ): Call<AirQuality>
}