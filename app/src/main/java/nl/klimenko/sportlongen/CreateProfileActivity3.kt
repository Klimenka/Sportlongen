package nl.klimenko.sportlongen


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import nl.klimenko.sportlongen.model.Patient
import java.util.*


class CreateProfileActivity3 : AppCompatActivity() {
    private lateinit var patient: Patient
    private lateinit var country: String
    private lateinit var city: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile3)
        patient = intent.getSerializableExtra("Patient") as Patient

        val cityField = findViewById<TextView>(R.id.city)
        val countryField = findViewById<TextView>(R.id.country)
        val nextButton = findViewById<ImageView>(R.id.next_button)
        val previousButton = findViewById<ImageView>(R.id.previous_button)

        if (patient.country != "") {
            countryField.text = patient.country
        }
        if (patient.city != "") {
            cityField.text = patient.city
        }

        val apiKey = getString(R.string.google_maps_key)
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey)
        }

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
        autocompleteFragment?.setTypeFilter(TypeFilter.CITIES)
        autocompleteFragment?.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS))
        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                findViewById<TextView>(R.id.error).visibility = View.GONE
                val placeAddressSplit = place.address.toString().split(",")
                country = placeAddressSplit[placeAddressSplit.size - 1]
                city = place.name.toString()

                countryField.text = country
                cityField.text = city
            }

            override fun onError(status: Status) {
                // TODO Handle the error.
                println("An error occurred: " + status);
            }
        })

        nextButton.setOnClickListener {
            country =
                countryField?.text.toString().trim()
            city =
                cityField?.text.toString().trim()
            if (country.isEmpty()) {
                findViewById<TextView>(R.id.error).visibility = View.VISIBLE
                return@setOnClickListener}


            patient.country = country
            patient.city = city
            intent = Intent(this, CreateProfileActivity4::class.java)
            intent.putExtra("Patient", patient)
            startActivity(intent)
            return@setOnClickListener
        }
        previousButton.setOnClickListener {
            this.finish()
        }
    }
}
