package nl.klimenko.sportlongen.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ResetPasswordRequest(
    @SerializedName("token") var token: String?,
    @SerializedName("password") var password: String?,
    @SerializedName("confirmPassword") var confirmPassword: String?
) : Serializable