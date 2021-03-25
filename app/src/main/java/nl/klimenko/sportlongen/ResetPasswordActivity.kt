package nl.klimenko.sportlongen

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.model.Patient
import nl.klimenko.sportlongen.model.ResetPasswordRequest
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity(), Callback<ResponseBody> {
    lateinit var sessionManager: SessionManager
    private val passwordLength = 8
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        sessionManager = SessionManager(this)

        loading = findViewById(R.id.loading)
        val resetCodeField = findViewById<EditText>(R.id.reset_code)
        val password1Field = findViewById<EditText>(R.id.password1)
        val password2Field = findViewById<EditText>(R.id.password2)
        val continueButton = findViewById<Button>(R.id.continue_button_password)

        password2Field.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view!!.windowToken, 0)
            }
        }
        continueButton.setOnClickListener {
            findViewById<TextView>(R.id.unknown_error).visibility = View.GONE
            findViewById<TextView>(R.id.passwords_not_match).visibility = View.GONE
            findViewById<TextView>(R.id.passwords_length_error).visibility = View.GONE
            val resetCode =
                resetCodeField?.text.toString().trim()
            val password1 =
                password1Field?.text.toString().trim()
            val password2 =
                password2Field?.text.toString().trim()
            if (password1.isEmpty()) {
                password1Field?.error =
                    getString(R.string.passwordRequired)
                password1Field?.requestFocus()
                return@setOnClickListener
            }
            if (password2.isEmpty()) {
                password2Field?.error =
                    getString(R.string.passwordRequired)
                password2Field?.requestFocus()
                return@setOnClickListener
            }
            if (resetCode.isEmpty()) {
                resetCodeField?.error =
                    getString(R.string.resetCodeRequired)
                password1Field?.requestFocus()
                return@setOnClickListener
            }

            if (checkPasswordsMatch(password1, password2)) {
                if (checkPasswordLength(password1)) {
                    loading.visibility = View.VISIBLE
                    val resetPasswordRequest = ResetPasswordRequest(resetCode, password1, password2)
                    RetrofitFactoryBackend.getBackendServiceObject()
                        .resetPassword(resetPasswordRequest = resetPasswordRequest)
                        .enqueue(this)
                    return@setOnClickListener
                } else {
                    findViewById<TextView>(R.id.passwords_length_error).visibility = View.VISIBLE
                }
            } else {
                findViewById<TextView>(R.id.passwords_not_match).visibility = View.VISIBLE
            }
            return@setOnClickListener
        }

    }

    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
        loading.visibility = View.GONE
        findViewById<TextView>(R.id.unknown_error).visibility = View.VISIBLE
        println("call to reset password got a failure")
    }

    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        loading.visibility = View.GONE
        if (response.code() == 200) {
            println("reset")
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        } else {
            findViewById<TextView>(R.id.unknown_error).visibility = View.VISIBLE
            println(response.code())
            println("call to reset password failed")
        }
    }
    private fun checkPasswordsMatch(password1: String, password2: String): Boolean {
        return password1 == password2
    }

    private fun checkPasswordLength(password: String): Boolean {
        return password.length >= passwordLength
    }
}