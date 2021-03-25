package nl.klimenko.sportlongen.model

import java.io.Serializable

data class LoginResponse (
    var accessToken:String,
    var refreshToken:String
) : Serializable