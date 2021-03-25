package nl.klimenko.sportlongen.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * This is the user object that is used with Patient or CareProvider
 */
data class User(
    @SerializedName("userId") var userId: Int?,
    @SerializedName("email") var email: String,
    @SerializedName("firstName") var firstName: String,
    @SerializedName("surName") var surName: String,
    @SerializedName("password") var password: String,
    @SerializedName("gender") var gender: Int,
    //it is Date in their Data model
    @SerializedName("birthday") var birthday: String,
    /*
        0: Patient
        1: CareProvider
     */
    @SerializedName("role") var role: Int?,
    @SerializedName("activated") var activated: Int?
) : Serializable