package nl.klimenko.sportlongen.model

import retrofit2.http.Field
import java.io.Serializable

/**
 * This object contains the information about the person how he/she felt during the exercise
 */
data class ExerciseLog(
    //  @Transient
    // var exerciseLogId: Int?,
    var completedExerciseId: Int,
    var howFeel: Int,
    var isAsthma: Int,
    var isHayFever: Int,
    var isLookingForward: Int?,
    var openReason: String?,
    @Field("heartrate")
    var heartrate: Int,
    var dateTime: String?
) : Serializable