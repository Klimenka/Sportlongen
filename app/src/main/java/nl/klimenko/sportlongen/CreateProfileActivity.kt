package nl.klimenko.sportlongen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import nl.klimenko.sportlongen.model.Patient


class CreateProfileActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var spinner: Spinner
    private lateinit var genders: Array<String>
    private lateinit var patient: Patient
    var firstName = ""
    var lastName = ""
    var gender = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        patient = intent.getSerializableExtra("Patient") as Patient
        genders = resources.getStringArray(R.array.other_gender)
        spinner = findViewById<View>(R.id.spinner) as Spinner
        val firstNameField = findViewById<EditText>(R.id.first_name)
        val lastNameField = findViewById<EditText>(R.id.last_name)
        val nextButton = findViewById<ImageView>(R.id.next_button)
        if (patient.user?.firstName != null && patient.user?.firstName != "") {
            firstNameField.setText(patient.user?.firstName)
        }
        if (patient.user?.surName != null && patient.user?.surName != "") {
            lastNameField.setText(patient.user?.surName)
        }
        //hide the keyboard
        lastNameField.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view!!.windowToken, 0)
            }
        }
        val adapterDefault = ArrayAdapter(
            this@CreateProfileActivity,
            R.layout.support_simple_spinner_dropdown_item, genders
        )

        adapterDefault.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner.adapter = adapterDefault
        spinner.onItemSelectedListener = this

        nextButton.setOnClickListener {
            firstName =
                firstNameField?.text.toString().trim()
            lastName =
                lastNameField?.text.toString().trim()
            if (firstName.isEmpty()) {
                firstNameField?.error =
                    getString(R.string.nameRequired)
                firstNameField?.requestFocus()
                return@setOnClickListener
            }
            if (lastName.isEmpty()) {
                lastNameField?.error =
                    getString(R.string.lastNameRequired)
                lastNameField?.requestFocus()
                return@setOnClickListener
            }
            patient.user?.firstName = firstName
            patient.user?.surName = lastName
            patient.user?.gender = gender
            intent = Intent(this, CreateProfileActivity2::class.java)
            intent.putExtra("Patient", patient)
            startActivity(intent)
            return@setOnClickListener
        }
    }

    override fun onItemSelected(
        parent: AdapterView<*>?,
        v: View?,
        position: Int,
        id: Long
    ) {
        gender = position
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        gender = 0
    }
}
