package nl.klimenko.sportlongen.model

import java.io.Serializable

/**
 * The feedback object that can give important information to a patient
 */
data class Feedback(
    var feedbackId: Int?,
    var careProviderId: Int,
    var message: String,
    var dateTime: String?
) : Serializable
