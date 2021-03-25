package nl.klimenko.sportlongen

import android.app.DatePickerDialog
import android.app.Dialog
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
import nl.klimenko.sportlongen.model.Register
import nl.klimenko.sportlongen.model.RegisterResponse
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity(), Callback<RegisterResponse> {
    lateinit var sessionManager: SessionManager
    private lateinit var loading: ProgressBar

    @RequiresApi(Build.VERSION_CODES.O)
    val dateTimeFormatter: SimpleDateFormat = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.getDefault())
    val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)
        sessionManager = SessionManager(this)
        loading = findViewById(R.id.loading_register)

        val actionCodeField = findViewById<EditText>(R.id.activation_code)
        val birthDayField = findViewById<TextView>(R.id.birthDay)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        birthDayField.setOnClickListener {
            val datePickerDialog =
                DatePickerDialog(this@RegisterActivity, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    birthDayField.text = ("" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year)
                }, year, month, day
                )
            datePickerDialog.getDatePicker().setMaxDate(Date().getTime())
            datePickerDialog.show()
        }
        val verify = findViewById<Button>(R.id.verify)
        //hide the keyboard
        actionCodeField.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view!!.windowToken, 0)
            }
        }
        verify.setOnClickListener {

            findViewById<TextView>(R.id.error_bad_credentials_register).visibility = View.GONE
            val activationCode =
                actionCodeField?.text.toString().trim()
            val birthDay =
                birthDayField?.text.toString().trim()
            if (activationCode.isEmpty()) {
                actionCodeField?.error =
                    getString(R.string.actionCodeRequired)
                actionCodeField?.requestFocus()
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
            val activationRequest = Register(dateFormated, activationCode)
            println(activationRequest)
            loading.visibility = View.VISIBLE
            RetrofitFactoryBackend.getBackendServiceObject()
                .registerUser(register = activationRequest)
                .enqueue(this)
            return@setOnClickListener
        }

    }

    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
        loading.visibility = View.GONE
        findViewById<TextView>(R.id.error_bad_credentials_register).visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
        loading.visibility = View.GONE
        if (response.code() == 200) {
            val registerResponse = response.body()!!
            val patient = registerResponse.patient
            val tokens = registerResponse.keys
            sessionManager.saveTokensFirstTime(tokens)
            patient.patientId?.let { sessionManager.saveUserId(it) }
            println(patient)
            println(tokens.accessToken)
            println(tokens.refreshToken)
            intent = Intent(this, PasswordActivity::class.java)
            intent.putExtra("Patient", patient)
            startActivity(intent)

        } else {
            findViewById<TextView>(R.id.error_bad_credentials_register).visibility = View.VISIBLE
        }
    }

}