package nl.klimenko.sportlongen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.model.Patient
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PasswordActivity : AppCompatActivity(), Callback<Patient> {
    lateinit var sessionManager: SessionManager
    private val passwordLength = 8
    private lateinit var patient : Patient
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_password)
        sessionManager = SessionManager(this)
        patient = intent.getSerializableExtra("Patient") as Patient
        loading = findViewById(R.id.loading_register)
        val email = findViewById<TextView>(R.id.email)
        email.text = patient.user?.email

        val password1Field = findViewById<EditText>(R.id.password1)
        val password2Field = findViewById<EditText>(R.id.password2)
        val continueButton = findViewById<Button>(R.id.continue_button_password)

        //hide the keyboard
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

            if (checkPasswordsMatch(password1, password2)) {
                if (checkPasswordLength(password1)) {
                    patient.user?.password = password1
                    loading.visibility = View.VISIBLE
                    RetrofitFactoryBackend.getBackendServiceObject()
                        .createProfile(token = SessionManager(this).fetchAuthToken(), patient = patient)
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

    private fun checkPasswordsMatch(password1: String, password2: String): Boolean {
        return password1 == password2
    }

    private fun checkPasswordLength(password: String): Boolean {
        return password.length >= passwordLength
    }
    override fun onFailure(call: Call<Patient>, t: Throwable) {
        findViewById<TextView>(R.id.unknown_error).visibility = View.VISIBLE
        loading.visibility = View.GONE
    }

    override fun onResponse(call: Call<Patient>, response: Response<Patient>) {
        loading.visibility = View.GONE
        if (response.code() == 200) {
            val patient = response.body()!!
            intent = Intent(this, CreateProfileActivity::class.java)
            intent.putExtra("Patient", patient)
            startActivity(intent)

        } else {
            findViewById<TextView>(R.id.unknown_error).visibility = View.VISIBLE
            println("call to create a profile failed")
        }
    }
}