package nl.klimenko.sportlongen

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.model.ForgotPasswordRequest
import nl.klimenko.sportlongen.model.Register
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.text.SimpleDateFormat
import java.util.*

class ForgotPasswordActivity : AppCompatActivity(), Callback<ResponseBody> {
    lateinit var sessionManager: SessionManager
    private lateinit var loading: ProgressBar
    @RequiresApi(Build.VERSION_CODES.O)
    val dateTimeFormatter: SimpleDateFormat = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.getDefault())
    val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        sessionManager = SessionManager(this)
        loading = findViewById(R.id.loading)
        val emailField = findViewById<EditText>(R.id.email)
        val birthDayField = findViewById<TextView>(R.id.birthDay)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        birthDayField.setOnClickListener {
            val datePickerDialog =
                DatePickerDialog(this@ForgotPasswordActivity, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    birthDayField.text = ("" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year)
                }, year, month, day
                )
            datePickerDialog.datePicker.maxDate = Date().getTime()
            datePickerDialog.show()
        }
        val verify = findViewById<Button>(R.id.next)
        //hide the keyboard
        emailField.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view!!.windowToken, 0)
            }
        }
        verify.setOnClickListener {

            findViewById<TextView>(R.id.error_bad_credentials).visibility = View.GONE
            val email =
                emailField?.text.toString().trim()
            val birthDay =
                birthDayField?.text.toString().trim()
            if (email.isEmpty()) {
                emailField?.error =
                    getString(R.string.emailRequired)
                emailField?.requestFocus()
                return@setOnClickListener
            }
            if (birthDay.isEmpty()) {
                birthDayField?.error =
                    getString(R.string.birthDayRequired)
                birthDayField?.requestFocus()
                return@setOnClickListener
            }
            val dateParsed = parser.parse(birthDay)
            val dateFormated = dateTimeFormatter.format(dateParsed as Date)
            val forgotPasswordRequest = ForgotPasswordRequest(email, dateFormated)
            loading.visibility = View.VISIBLE
            RetrofitFactoryBackend.getBackendServiceObject()
                .forgetPassword(forgotPasswordRequest = forgotPasswordRequest)
                .enqueue(this)
            return@setOnClickListener
        }
    }

    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
        findViewById<TextView>(R.id.error_bad_credentials).visibility = View.VISIBLE
    }

    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        loading.visibility = View.GONE
        if (response.code() == 200) {
           intent = Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
            startActivity(intent)

        } else {
            findViewById<TextView>(R.id.error_bad_credentials).visibility = View.VISIBLE
        }
    }
}