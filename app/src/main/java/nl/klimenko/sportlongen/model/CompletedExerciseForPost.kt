package nl.klimenko.sportlongen.model

import java.io.Serializable

/**
 * This object is created to do post requests
 */
data class CompletedExerciseForPost(
    var exerciseTypeId: Int?,
    var patientId: Int?,
    var startDateTime: String?,
    var endDateTime: String?,
    var maxHeartrate: Int?,
    var minHeartrate: Int?,
    var avgHeartrate: Int?,
    var gpsData: GpsDataPost?,
    var temperature: Int?,
    var humidity: Int?,
    var airpolution: Int?,
    var distance: Double?,
    var feedback: Feedback?,
    var maxPace: Double?,
    var minPace: Double?,
    var avgPace: Double?
) : Serializable