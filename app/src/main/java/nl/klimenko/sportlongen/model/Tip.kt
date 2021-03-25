package nl.klimenko.sportlongen.model

import java.io.Serializable

/**
 * The tip object that can give important information to an patient
 */
data class Tip(
    var tipId: String?,
    var title: String,
    var description: String,
    var image: Image?,
    var categoryId: Int,
    var category: TipCategory?
) : Serializable