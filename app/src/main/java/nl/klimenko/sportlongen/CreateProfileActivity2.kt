package nl.klimenko.sportlongen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import nl.klimenko.sportlongen.model.Patient

class CreateProfileActivity2 : AppCompatActivity() {
    private lateinit var patient : Patient
    var height = 0
    var weight = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile2)
        patient = intent.getSerializableExtra("Patient") as Patient

        val heightField = findViewById<EditText>(R.id.height)
        val weightField = findViewById<EditText>(R.id.weight)
        val nextButton = findViewById<ImageView>(R.id.next_button)
        val previousButton = findViewById<ImageView>(R.id.previous_button)

        if (patient.height != 0) {
            heightField.setText(patient.height.toString())
        }
        if (patient.weight != 0.0) {
            weightField.setText(patient.weight.toString())
        }
        //hide the keyboard
        weightField.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view!!.windowToken, 0)
            }
        }
        nextButton.setOnClickListener {
            val heightRead =
                heightField?.text.toString().trim()
            val weightRead =
                weightField?.text.toString().trim()
            if (heightRead.isEmpty()) {
                heightField?.error =
                    getString(R.string.heightRequired)
                heightField?.requestFocus()
                return@setOnClickListener
            } else{
                height = heightField?.text.toString().trim().toInt()
            }
            if (weightRead.isEmpty()) {
                weightField?.error =
                    getString(R.string.weightRequired)
                weightField?.requestFocus()
                return@setOnClickListener
            } else{
                weight = weightField?.text.toString().trim().toDouble()
            }
            println(height)
            println(weight)
            patient.height = height
            patient.weight = weight
            intent = Intent(this, CreateProfileActivity3::class.java)
            intent.putExtra("Patient", patient)
            startActivity(intent)
            return@setOnClickListener
        }
        previousButton.setOnClickListener {
            this.finish()
        }
    }
}