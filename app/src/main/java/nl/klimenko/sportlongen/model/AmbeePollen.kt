package nl.klimenko.sportlongen.model

import java.io.Serializable
/*
Model for Ambee API response
 */
data class AmbeePollen(
    val message: String,
    val data: List<Data>
) : Serializable

data class Data(
    val Count: Count,
    val Risk: Risk

) : Serializable

data class Count(
    val grass_pollen: Int,
    val tree_pollen: Int,
    val weed_pollen: Int
) : Serializable

data class Risk(
    val grass_pollen: String,
    val tree_pollen: String,
    val weed_pollen: String
) : Serializable