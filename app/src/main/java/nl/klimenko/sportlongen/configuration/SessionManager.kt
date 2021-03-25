package nl.klimenko.sportlongen.configuration

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import nl.klimenko.sportlongen.R
import nl.klimenko.sportlongen.model.LoginResponse
import org.json.JSONObject
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.util.*


class SessionManager(context: Context) : Serializable {

    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val USER_NAME = "user_name"
        const val USER_ID = "user_id"
        const val USER_CHOICE_NOTIFICATION = "user_choice_notification"
        const val USER_CHOICE_VOICE_NOTIFICATION = "user_voice_notification"
        const val USER_CHOICE_NOTIFICATION_NEVER_ASK = "user_choice_notification_never_ask"
        const val USER_CHOICE_INHALER_NEVER_ASK = "user_choice_inhaler_never_ask"
        const val KEY_FOR_JSON_USER_ID = "http://schemas.microsoft.com/ws/2008/06/identity/claims/userdata"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTokens(tokens: LoginResponse) {
        saveAuthToken(tokens.accessToken)
        saveRefreshToken(tokens.refreshToken)
        saveIdFromToken(tokens.accessToken)
    }
    fun saveTokensFirstTime(tokens: LoginResponse) {
        saveAuthToken(tokens.accessToken)
        saveRefreshToken(tokens.refreshToken)
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        val tokenString = "Bearer " + token
        editor.putString(USER_TOKEN, tokenString)
        editor.apply()
    }

    fun saveRefreshToken(token: String?) {
        val editor = prefs.edit()
        editor.putString(REFRESH_TOKEN, token)
        editor.apply()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun saveIdFromToken(token: String) {
       val id = decodeTokenGetId(token)
        if(id != 0){
            saveUserId(id)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decodeTokenGetId(token: String) : Int {
        var id = 0
        val parts = token.split(("\\.").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size > 1) {
            val bytes = Base64.getUrlDecoder().decode(parts[1])
            val decodedString = String(bytes, StandardCharsets.UTF_8)
            val json = JSONObject(decodedString)
           try{
               id = Integer.parseInt(json.get(KEY_FOR_JSON_USER_ID).toString())
           }
           catch (e: ClassCastException){
               println(e.message)
           }

        }
        return id
    }

    fun saveUserId(id: Int) {
        val editor = prefs.edit()
        editor.putInt(USER_ID, id)
        editor.apply()
    }

    fun fetchUserId(): Int {
        return prefs.getInt(USER_ID, -1)
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun fetchRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN, null)
    }

    fun clearUserCredentials() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()

    }

    fun saveUserChoiceNotification(choice: Boolean) {
        val editor = prefs.edit()
        editor.putString(USER_CHOICE_NOTIFICATION, choice.toString())
        editor.apply()
    }

    fun fetchUserChoiceNotification(): Boolean? {
        val choice = prefs.getString(USER_CHOICE_NOTIFICATION, null)
        return choice?.toBoolean()
    }

    fun saveUserChoiceVoiceNotification(choice: Boolean) {
        val editor = prefs.edit()
        editor.putString(USER_CHOICE_VOICE_NOTIFICATION, choice.toString())
        editor.apply()
    }

    fun fetchUserChoiceVoiceNotification(): Boolean? {
        val choice = prefs.getString(USER_CHOICE_VOICE_NOTIFICATION, null)
        return choice?.toBoolean()
    }

    fun saveUserChoiceNotificationNeverAsk(choice: Boolean) {
        val editor = prefs.edit()
        editor.putString(USER_CHOICE_NOTIFICATION_NEVER_ASK, choice.toString())
        editor.apply()
    }

    fun fetchUserChoiceNotificationNeverAsk(): Boolean? {
        val choice = prefs.getString(USER_CHOICE_NOTIFICATION_NEVER_ASK, null)
        return choice?.toBoolean()
    }

    fun saveUserChoiceInhalerNotificationNeverAsk(choice: Boolean) {
        val editor = prefs.edit()
        editor.putString(USER_CHOICE_INHALER_NEVER_ASK, choice.toString())
        editor.apply()
    }

    fun fetchUserChoiceInhalerNotificationNeverAsk(): Boolean? {
        val choice = prefs.getString(USER_CHOICE_INHALER_NEVER_ASK, null)
        return choice?.toBoolean()
    }


    /**
    check if the user asked not to show the notifications
    true - not show
    false - show
     **/
    fun checkIfNotShowNotifications(): Boolean {
        return if (fetchUserChoiceNotificationNeverAsk() != null) {
            fetchUserChoiceNotificationNeverAsk()!!
        } else {
            false
        }
    }

    /**
    check if the user asked not to show the inhaler notifications
    true - not show
    false - show
     **/
    fun checkIfNotShowInhalerNotifications(): Boolean {
        return if (fetchUserChoiceInhalerNotificationNeverAsk() != null) {
            fetchUserChoiceInhalerNotificationNeverAsk()!!
        } else {
            false
        }
    }
}
