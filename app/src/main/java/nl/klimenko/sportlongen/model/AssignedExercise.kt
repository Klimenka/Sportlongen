package nl.klimenko.sportlongen.model

import retrofit2.http.Field
import java.io.Serializable

/**
 * This object contains the information and recommendations for a certain type of
 * exercise for the patient.
 */
data class AssignedExercise(
    var assignedExerciseId: Int?,
    var exerciseTypeId: Int,
    var exerciseType: ExerciseType,
    var patientId: Int,
    var maxDuration: String,
    var maxHeartRate: Int,
    var maxPace: Int,
    @Field("maxAirpolution")
    var maxAirPollution: Int,
    var maxHumidity: Int,
    var maxTemperature: Int,
    var timesAWeek: Int,
    var maxDistance: Double
) : Serializable