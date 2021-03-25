package nl.klimenko.sportlongen


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend

import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback

class LogoutActivity : BaseActivity(), Callback<ResponseBody> {
    lateinit var loading: ProgressBar
    lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loading = findViewById(R.id.loading)
        sessionManager = SessionManager(this)
        val logout = findViewById<ImageView>(R.id.logout_button)
        logout.setOnClickListener {
            loading.visibility = View.VISIBLE
            RetrofitFactoryBackend.getBackendServiceObject()
                .logout(token = SessionManager(this).fetchAuthToken())
                .enqueue(this)
        }
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_logout
    }

    override fun getNavigationMenuItemId(): Int {
        return R.id.profileMenuButton
    }

    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
        Toast.makeText(this, this.getString(R.string.a_network_error_occurred_please_try_again), Toast.LENGTH_LONG).show()
        loading.visibility = View.GONE
        println("Logout failed")
    }

    override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
        loading.visibility = View.GONE
        if (response.isSuccessful && response.body() != null) {
            println("got logout")
            sessionManager.saveAuthToken("")
            sessionManager.saveRefreshToken("")
            intent = Intent(this@LogoutActivity, GreetingActivity::class.java)
            startActivity(intent)

        } else {
            println("got an empty response from logout")
            Toast.makeText(this, this.getString(R.string.a_network_error_occurred_please_try_again), Toast.LENGTH_LONG).show()
        }
    }
}