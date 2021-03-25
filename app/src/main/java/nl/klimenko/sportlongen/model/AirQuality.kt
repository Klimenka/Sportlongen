package nl.klimenko.sportlongen.model

import java.io.Serializable
/*
Model for OpenWeatherMap API response
 */
data class AirQuality(
    val list: List<ListAirQuality>
) : Serializable

data class ListAirQuality(
    val dt: Long,
    val main: MainAirQuality,
    val components: ComponentAirQuality
) : Serializable

data class MainAirQuality(
    /*
     Air Quality Index. Possible values: 1, 2, 3, 4, 5. Where 1 = Good, 2 = Fair, 3 = Moderate, 4 = Poor, 5 = Very Poor.
     */
    val aqi: Int
) : Serializable

data class ComponentAirQuality(
    val co: Double,
    val no: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    val pm2_5: Double,
    val pm10: Double,
    val nh3: Double
) : Serializable
