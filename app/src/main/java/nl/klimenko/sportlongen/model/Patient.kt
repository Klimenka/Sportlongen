package nl.klimenko.sportlongen.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * The patient that has the most important role in this application
 */
data class Patient(
    @SerializedName("patientId") var patientId: Int?,
    @SerializedName("user") var user: User?,
    @SerializedName("height") var height: Int,
    @SerializedName("weight") var weight: Double,
    /*
        example: 2
        The degree of asthma a patient has form 0-4 ("I'm not sure", "Intermittent", "Mild persistent", "Moderate persistent", "Severe persistent")
     */
    @SerializedName("asthmaLevel") var asthmaLevel: Int,
    @SerializedName("careProviderId") var careProviderId: Int?,
    @SerializedName("country") var country: String,
    @SerializedName("city") var city: String,
    @SerializedName("assignedExercises") var assignedExercises: List<AssignedExercise>?,
    @SerializedName("completedExercisesThisWeek") var completedExercisesThisWeek: List<CompletedExercise>?
) : Serializable