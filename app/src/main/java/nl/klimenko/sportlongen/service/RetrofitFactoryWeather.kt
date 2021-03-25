package nl.klimenko.sportlongen.service

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitFactoryWeather {
    lateinit var weatherService: WeatherService
    private var retrofit: Retrofit? = null

    private fun buildRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    fun getRetrofitObject(): Retrofit? {
        return if (retrofit == null) {
            buildRetrofit()
            retrofit
        } else {
            retrofit
        }
    }

    fun getWeatherServiceObject(): WeatherService {
        return if (this::weatherService.isInitialized) {
            weatherService
        } else {
            getRetrofitObject()?.create(WeatherService::class.java)!!
        }
    }
}