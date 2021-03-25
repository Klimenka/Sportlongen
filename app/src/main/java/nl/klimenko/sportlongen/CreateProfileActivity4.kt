package nl.klimenko.sportlongen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.model.Patient
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateProfileActivity4 : AppCompatActivity(), Callback<Patient> {
    private lateinit var patient: Patient
    private lateinit var loading: ProgressBar
    private var levelOfAsthma = 0
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile4)
        patient = intent.getSerializableExtra("Patient") as Patient
        loading = findViewById(R.id.loading_register)
        sessionManager = SessionManager(this)
        val nextButton = findViewById<ImageView>(R.id.next_button)
        val previousButton = findViewById<ImageView>(R.id.previous_button)
        findViewById<RadioGroup>(R.id.radio_group).setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { _, i ->
            //this id is weird because it is generated after setting an id for this element in xml
            if (i != 2131362246) {
                findViewById<RadioButton>(R.id.radio1).isChecked = false
            } else {
                levelOfAsthma = 0
            }
            levelOfAsthma = i
        })
        nextButton.setOnClickListener {
            patient.asthmaLevel = levelOfAsthma
            loading.visibility = View.VISIBLE
            RetrofitFactoryBackend.getBackendServiceObject()
                .createProfile(token = SessionManager(this).fetchAuthToken(), patient = patient)
                .enqueue(this)
            return@setOnClickListener
        }
        previousButton.setOnClickListener {
            this.finish()
        }


    }

    override fun onFailure(call: Call<Patient>, t: Throwable) {
        //TODO make an entry in internal DB that holds this patient object
        loading.visibility = View.GONE
        intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onResponse(call: Call<Patient>, response: Response<Patient>) {
        loading.visibility = View.GONE
        if (response.code() == 200) {
            val patient = response.body()!!
            patient.patientId?.let { sessionManager.saveUserId(it) }
            println(patient)
            intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

        } else {
            println(response.code())
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            println("call to create a profile failed")
        }
    }

}