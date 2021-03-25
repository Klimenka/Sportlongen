package nl.klimenko.sportlongen.model

import java.io.Serializable

/**
 * The image objects that is within a tip
 */
data class Image(
    var name: String?,
    var altName: String,
    var url: String?
) : Serializable