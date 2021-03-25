package nl.klimenko.sportlongen.model

import java.io.Serializable

/**
 * This is the type of exercise that is available
 */
data class ExerciseType(
    var exerciseTypeId: Int?,
    var title: String,
    var description: String,
    var deletedAt: String?
) : Serializable