package nl.klimenko.sportlongen.configuration

object WeatherCache {
    var myTemperature: Int = 0
    var myHumidity: Int = 0
    var myAirPollution: Int = 0
    private var linkToIcon: String = ""
    var pollenRisk: String = ""
    fun setTemperature(temperature: Int) {
        myTemperature = temperature
    }

    fun setHumidity(humidity: Int) {
        myHumidity = humidity
    }

    fun setAirPollution(airPollution: Int) {
        myAirPollution = airPollution
    }

    fun setLinkToIcon(link: String) {
        linkToIcon = link
    }

    fun getLinkToIcon(): String {
        return linkToIcon
    }

}