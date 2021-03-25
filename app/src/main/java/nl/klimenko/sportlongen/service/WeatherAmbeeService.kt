package nl.klimenko.sportlongen.service

import nl.klimenko.sportlongen.model.AmbeePollen
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface WeatherAmbeeService {

    @GET("/latest/pollen/by-lat-lng")
    fun getPollenWeather(
        @Query("lat") lat: Double?,
        @Query("lng") lng: Double?,
        @Header("x-api-key") appId: String
    ): Call<AmbeePollen>

}