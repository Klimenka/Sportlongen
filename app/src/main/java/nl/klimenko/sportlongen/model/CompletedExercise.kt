package nl.klimenko.sportlongen.model

import retrofit2.http.Field
import java.io.Serializable

/**
 * This object contains almost all data from the completed exercise an patient did
 */
data class CompletedExercise(
    var completedExerciseId: Int?,
    var exerciseTypeId: Int?,
    var exerciseType: ExerciseType?,
    var patientId: Int?,
    var startDateTime: String?,
    var endDateTime: String?,
    @Field("maxHeartrate")
    var maxHeartRate: Int?,
    @Field("minHeartrate")
    var minHeartRate: Int?,
    @Field("avgHeartrate")
    var avgHeartRate: Int?,
    var gpsDataId: String?,
    var gpsData: GpsData?,
    var temperature: Int?,
    var humidity: Int?,
    @Field("airpolution")
    var airPollution: Int?,
    var distance: Double?,
    var feedbackId: Int?,
    var feedback: Feedback?,
    var exerciseLogs: List<ExerciseLog>?,
    var maxPace: Double?,
    var minPace: Double?,
    var avgPace: Double?
) : Serializable