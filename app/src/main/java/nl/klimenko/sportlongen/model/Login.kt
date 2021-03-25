package nl.klimenko.sportlongen.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Login(
    @SerializedName("email") var email: String?,
    @SerializedName("password") var password: String?
) : Serializable