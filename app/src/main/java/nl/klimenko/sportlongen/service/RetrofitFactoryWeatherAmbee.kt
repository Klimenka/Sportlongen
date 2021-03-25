package nl.klimenko.sportlongen.service

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitFactoryWeatherAmbee {
    private var retrofit: Retrofit? = null
    lateinit var weatherAmbeeService: WeatherAmbeeService

    private fun buildRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl(" https://api.ambeedata.com/")
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

    fun getWeatherServiceObject(): WeatherAmbeeService {
        return if (this::weatherAmbeeService.isInitialized) {
            weatherAmbeeService
        } else {
            getRetrofitObject()?.create(WeatherAmbeeService::class.java)!!
        }
    }
}