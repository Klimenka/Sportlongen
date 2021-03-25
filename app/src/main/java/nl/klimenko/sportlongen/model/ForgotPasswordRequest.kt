package nl.klimenko.sportlongen.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ForgotPasswordRequest (
        @SerializedName("email") var email: String?,
        @SerializedName("birthDate") var birthDate: String?

    ) : Serializable
