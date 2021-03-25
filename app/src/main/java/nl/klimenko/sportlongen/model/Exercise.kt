package nl.klimenko.sportlongen.model

import java.io.Serializable
/*
* This object is used to send the limits to the exercise
* */
data class Exercise(
    var exerciseName: String,
    var targetDistance: Double?,
    var durationInMinutes: Int?,
    var maxPaceInSeconds: Int?,
    var maxHeartRate: Int?
) : Serializable