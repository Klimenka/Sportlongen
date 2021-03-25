package nl.klimenko.sportlongen.model

import retrofit2.http.Field
import java.io.Serializable

/**
 * A point that is taken during the exercise the patient was doing
 */
data class LocationPoint(
    var dateTime: String?,
    var latitude: Double?,
    @Field("longtitude")
    var longtitude: Double?
) : Serializable