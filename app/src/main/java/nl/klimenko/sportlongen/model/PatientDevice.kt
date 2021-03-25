package nl.klimenko.sportlongen.model

import java.io.Serializable

/**
 * The category a tip object belongs to
 */
data class PatientDevice(
    var patientDeviceId: Int?,
    var patientId: Int,
    var firebaseDeviceToken: String
) : Serializable