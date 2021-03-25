package nl.klimenko.sportlongen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import nl.klimenko.sportlongen.configuration.ContextObject
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.model.LoginResponse
import nl.klimenko.sportlongen.model.RefreshTokenRequest
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ContextObject.setup(this)
        sessionManager = ContextObject.context?.let { SessionManager(it) }!!

        val backgroundImage: ImageView = findViewById(R.id.run_man_1)
        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.side_slide)
        backgroundImage.visibility = View.VISIBLE
        backgroundImage.startAnimation(slideAnimation)


        Handler(Looper.getMainLooper()).postDelayed({
            findViewById<ImageView>(R.id.run_man_1).visibility = View.GONE
            val logoImage = findViewById<ImageView>(R.id.logo)
            val slideAnimation2 = AnimationUtils.loadAnimation(this, R.anim.side_slide)
            logoImage.startAnimation(slideAnimation2)
            logoImage.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({ checkIfRefreshTokenIsValid() }, 1500)

        }, 1500)


    }

    fun openStartPage() {
        val intent = Intent(this, GreetingActivity::class.java)
        startActivity(intent)
    }

    private fun checkIfRefreshTokenIsValid() {

        val refreshTokenRequest = sessionManager.fetchRefreshToken()
        if (refreshTokenRequest != null) {
            RetrofitFactoryBackend.getBackendServiceObject()
                .refreshToken(true, RefreshTokenRequest(refreshTokenRequest))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        println("failure to get a token, open Login page")
                        openStartPage()
                        //loading.visibility = View.GONE
                    }

                    override fun onResponse(
                        call: Call<LoginResponse>, response: Response<LoginResponse>
                    ) {

                        when {
                            response.code() == 200 -> {
                                response.body()?.refreshToken?.let {
                                    sessionManager.saveRefreshToken(
                                        it
                                    )
                                }
                                response.body()?.accessToken?.let { sessionManager.saveAuthToken(it) }
                                println("new tokens saved")
                                val intent2 = Intent(this@MainActivity, HomeActivity::class.java)
                                startActivity(intent2)
                            }
                            response.code() == 401 -> {
                                //  loading.visibility = View.GONE
                                println("unauthorized")
                                openStartPage()
                            }
                            else -> {
                                //   loading.visibility = View.GONE
                                println("got an empty response from get a patient call")
                                openStartPage()
                            }
                        }
                    }
                })
        } else {
            openStartPage()
        }
    }
}
