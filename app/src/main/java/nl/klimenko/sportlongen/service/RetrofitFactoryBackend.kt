package nl.klimenko.sportlongen.service

import android.os.Build
import androidx.annotation.RequiresApi
import nl.klimenko.sportlongen.configuration.ContextObject
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.model.RefreshTokenRequest
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


object RetrofitFactoryBackend {
    private var retrofit: Retrofit? = null
    lateinit var backendService: BackendService

    private fun buildRetrofit() {
        val okHttpClient =
            OkHttpClient().newBuilder()
                .authenticator(TokenAuthenticator())
                .build()

        retrofit = Retrofit.Builder()
            .baseUrl("https://asthma-local-release.azurewebsites.net/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    private fun getRetrofitObject(): Retrofit? {
        return if (retrofit == null) {
            buildRetrofit()
            retrofit
        } else {
            retrofit
        }
    }

    fun getBackendServiceObject(): BackendService {
        return if (this::backendService.isInitialized) {
            backendService
        } else {
            getRetrofitObject()?.create(BackendService::class.java)!!
        }
    }

}
/*
    This Authenticator calls when the response code is 401, which can mean that the access token is not valid
    By calling this authenticate method Retrofit updates access token using refresh token and repeats the last call
 */
class TokenAuthenticator : Authenticator {
    //check if that was a request not for refresh token, prevent from looping with 401 response code
    private fun isEligibleForRefresh(originalResponse: Response) =
        originalResponse.request.header("x-no-refresh") != true.toString()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun authenticate(route: Route?, response: Response): Request? {
        if (!isEligibleForRefresh(response)) {
            return null
        }
        val updatedToken = getNewToken()
        //if got a new access token repeat the last request
        return if (updatedToken) {
            ContextObject.context?.let {
                SessionManager(it).fetchAuthToken()?.let {
                    response.request.newBuilder().header("Authorization", it)
                        .build()
                }
            }
        } else {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNewToken(): Boolean {
        val sessionManager = ContextObject.context?.let { SessionManager(it) }

        val refreshTokenRequest = sessionManager?.fetchRefreshToken()
        if (refreshTokenRequest != null) {
            val call = RetrofitFactoryBackend.getBackendServiceObject()
                .refreshToken(true, RefreshTokenRequest(refreshTokenRequest))
            val authTokenResponse = call.execute()
            println(authTokenResponse.code())
            return if (authTokenResponse.code() != 200) {
                //val intent = Intent(ContextObject.context, LoginActivity::class.java)
              //  ContextObject.context?.startActivity(intent)
                //logout
           //     sessionManager.saveRefreshToken(null)
                false
            } else {
                authTokenResponse.body()?.let { sessionManager.saveTokens(it)}
                true
            }
        }
     //   val intent = Intent(ContextObject.context, LoginActivity::class.java)
       // ContextObject.context?.startActivity(intent)
        return false

    }
}





