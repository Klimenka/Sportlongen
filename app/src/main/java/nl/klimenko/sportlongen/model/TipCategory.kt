package nl.klimenko.sportlongen.model

import java.io.Serializable

/**
 * The category a tip object belongs to
 */
data class TipCategory(
    var tipCategoryId: String?,
    var name: String
) : Serializable