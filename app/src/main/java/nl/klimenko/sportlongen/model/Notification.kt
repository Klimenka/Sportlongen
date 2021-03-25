package nl.klimenko.sportlongen.model

import java.io.Serializable

/**
 * This object contains the notifications for the patient, from the care provider
 * to motivate the patient to exercise
 */
data class Notification(
    var notificationId: Int?,
    var patientId: Int?,
    var patient: Patient?,
    var message: String
) : Serializable