package nl.klimenko.sportlongen.model

import java.io.Serializable

/**
 * The GPS data objects contains a list of location points
 */
data class GpsDataPost(
    var locationPoints: List<LocationPoint>?
) : Serializable