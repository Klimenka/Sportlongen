package nl.klimenko.sportlongen


import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.model.Login
import nl.klimenko.sportlongen.model.LoginResponse
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity(), Callback<LoginResponse> {
    lateinit var sessionManager: SessionManager
    private lateinit var loading: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        sessionManager = SessionManager(this)
        loading = findViewById(R.id.loading)

        val usernameField = findViewById<EditText>(R.id.username)
        val passwordField = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
        val register = findViewById<Button>(R.id.register)
        val forgotPassword = findViewById<TextView>(R.id.forgot_password_button)
        forgotPassword.setOnClickListener {
            intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        register.setOnClickListener{
            val intent1 = Intent(this, RegisterActivity::class.java)
            startActivity(intent1)
        }
        passwordField.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view!!.windowToken, 0)
            }
        }
        login.setOnClickListener {

            findViewById<TextView>(R.id.error_bad_credentials).visibility = View.GONE
            val userName =
                usernameField?.text.toString().trim()
            val password =
                passwordField?.text.toString().trim()
            if (userName.isEmpty()) {
                usernameField?.error =
                    getString(R.string.userNameRequired)
                usernameField?.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordField?.error =
                    getString(R.string.passwordRequired)
                passwordField?.requestFocus()
                return@setOnClickListener
            }
            val loginRequest = Login(userName, password)
            println(loginRequest)
            loading.visibility = View.VISIBLE
            RetrofitFactoryBackend.getBackendServiceObject()
                .login(login = loginRequest)
                .enqueue(this)
            return@setOnClickListener
        }

    }

    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
        loading.visibility = View.GONE
        findViewById<TextView>(R.id.error_bad_credentials).visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
        loading.visibility = View.GONE
        if (response.code() == 200) {
            val tokens = response.body()!!
            sessionManager.saveTokens(tokens)
            intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        } else {
            findViewById<TextView>(R.id.error_bad_credentials).visibility = View.VISIBLE
        }
    }

}
