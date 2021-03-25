package nl.klimenko.sportlongen.model
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RegisterResponse (
    var patient: Patient,
    var keys: LoginResponse
) : Serializable

data class Register(
    @SerializedName("birthDate") var birthDate: String?,
    @SerializedName("activationCode") var activationCode: String?
) : Serializable