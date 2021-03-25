package nl.klimenko.sportlongen.configuration

import java.text.SimpleDateFormat
import java.util.*

object HelperConverter {

    fun secondsToMinutesString(secondsInput: Int): String {
        val minutes = secondsInput / 60
        val seconds = secondsInput % 60
        return if (minutes >= 60) {
            ">" + java.lang.String.format("%02d", 59) + ":" + java.lang.String.format(
                "%02d",
                seconds
            )
        } else {
            java.lang.String.format("%02d", minutes) + ":" + java.lang.String.format(
                "%02d",
                seconds
            )
        }
    }

    fun minutesStringToSeconds(minutes: String): Int {
        val parser = SimpleDateFormat("mm:ss", Locale.getDefault())
        return (parser.parse(minutes).time / 1000).toInt()

    }

    fun minutesIntToHoursMinutesSecondsString(minutes: Int): String {
        val hours = minutes / 60
        val minutes = minutes % 60
        return java.lang.String.format("%02d", hours) + ":" + java.lang.String.format(
            "%02d",
            minutes
        ) + ":" + java.lang.String.format("%02d", 0)
    }
}