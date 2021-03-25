package nl.klimenko.sportlongen.model

import java.io.Serializable

/**
 * This is the Care Provider that keeps track of the patient
 */
data class CareProvider(
    var careProviderId: Int?,
    var user: User?,
    var jobTitle: String?
) : Serializable