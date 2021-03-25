package nl.klimenko.sportlongen


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.gson.Gson
import nl.klimenko.sportlongen.adapters.GoalsAdapter
import nl.klimenko.sportlongen.configuration.ImageActivity.getImageForActivityType
import nl.klimenko.sportlongen.configuration.MyLocationManager
import nl.klimenko.sportlongen.configuration.PatientCache
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.configuration.WeatherCache
import nl.klimenko.sportlongen.model.*
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import nl.klimenko.sportlongen.service.RetrofitFactoryWeather
import nl.klimenko.sportlongen.service.RetrofitFactoryWeatherAmbee
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class HomeActivity : BaseActivity(), LocationListener {
    var patient: Patient? = null
    private lateinit var sessionManager: SessionManager
    lateinit var loading: ProgressBar

    private fun getLocationAndRequestWeather() {
        val location = MyLocationManager.getLocationOnce(this@HomeActivity)
        if (location != null) {
            getWeather(location)
            getAmbeePollenWeather(location)
            getAirQuality(location)
        } else {
            //Todo ask weather by city!!!!
            println("Location is null")
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loading = findViewById(R.id.loading_home)
        sessionManager = SessionManager(this)
        getPatient()
        getLocationAndRequestWeather()

    }

    override fun getContentViewId(): Int {
        return R.layout.activity_home
    }

    override fun getNavigationMenuItemId(): Int {
        return R.id.homeMenuButton
    }

    private fun getPatient() {
        loading.visibility = View.VISIBLE
        RetrofitFactoryBackend.getBackendServiceObject()
            .getPatient(sessionManager.fetchAuthToken(), sessionManager.fetchUserId())
            .enqueue(object : Callback<Patient> {
                override fun onFailure(call: Call<Patient>, t: Throwable) {
                    //maybe got something from the save state
                    println("failure to get a patient data")
                    val intent1 = Intent(this@HomeActivity, LoginActivity::class.java)
                    startActivity(intent1)
                    loading.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<Patient>, response: Response<Patient>
                ) {
                    when {
                        response.code() == 200 -> {
                            println("got an object of a user")
                            patient = response.body()!!
                            PatientCache.patient = patient
                            patient!!.patientId?.let { sessionManager.saveUserId(it) }
                            loading.visibility = View.GONE
                            //update the view
                            updateUserName(patient!!)
                            if (patient!!.assignedExercises != null && patient!!.assignedExercises?.size!!>0) {
                                updateGoals(
                                    patient!!.assignedExercises as List<AssignedExercise>,
                                    patient!!.completedExercisesThisWeek
                                )
                            }
                            if (patient!!.completedExercisesThisWeek != null && patient!!.completedExercisesThisWeek?.size!! > 0) {
                                updateTheLastExercise(patient!!.completedExercisesThisWeek)
                            }
                            else{
                                updateLastExercisesEmpty()
                            }

                        }
                        response.code() == 401 -> {
                            loading.visibility = View.GONE
                            val intent1 = Intent(this@HomeActivity, LoginActivity::class.java)
                            println("unauthorized")
                            startActivity(intent1)
                        }
                        else -> {
                            loading.visibility = View.GONE
                            println("got an empty response from get a patient call")
                        }
                    }
                }
            })
    }

    private fun getWeather(location: Location) {
        try {
            RetrofitFactoryWeather.getWeatherServiceObject().getWeather(
                location.latitude,
                location.longitude,
                getString(R.string.units),
                getString(R.string.apiKeyOWMWeather)
            )
                .enqueue(object : Callback<WeatherForecast> {
                    override fun onFailure(call: Call<WeatherForecast>, t: Throwable) {
                        println("weather forecast call failed Open Weather Map")
                    }

                    override fun onResponse(
                        call: Call<WeatherForecast>,
                        response: Response<WeatherForecast>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            println("Successful request to weather api")

                            val weather = response.body()!!
                            updateTemperatureHumidity(weather)

                        } else {
                            println(response.errorBody())
                        }
                    }
                })
        } catch (e: NullPointerException) {
            print(e.message)
            println("weather API gives null pointer exception because the currentLocation is null")
        }
    }
    private fun getAirQuality(location: Location) {
        try {
            RetrofitFactoryWeather.getWeatherServiceObject().getAirQuality(
                location.latitude,
                location.longitude,
                getString(R.string.apiKeyOWMWeather)
            )
                .enqueue(object : Callback<AirQuality> {
                    override fun onFailure(call: Call<AirQuality>, t: Throwable) {
                        println("weather forecast call failed Open Weather Map")
                        println(t.message)
                    }

                    override fun onResponse(
                        call: Call<AirQuality>,
                        response: Response<AirQuality>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            println("Successful request to weather api")

                            val weather = response.body()!!
                            updateAirQualityView(weather.list[0].main.aqi)
                            println(weather.list[0].main.aqi)
                        } else {
                            println(response.errorBody())
                        }
                    }
                })
        } catch (e: NullPointerException) {
            print(e.message)
            println("weather API gives null pointer exception because the currentLocation is null")
        }
    }

    private fun getAmbeePollenWeather(location: Location) {
        RetrofitFactoryWeatherAmbee.getWeatherServiceObject().getPollenWeather(
            location.latitude,
            location.longitude,
            getString(R.string.apiKeyAmbee)
        )
            .enqueue(object : Callback<AmbeePollen> {
                override fun onFailure(call: Call<AmbeePollen>, t: Throwable) {
                    println("weather forecast call failed Ambee Pollen")
                }

                override fun onResponse(
                    call: Call<AmbeePollen>,
                    response: Response<AmbeePollen>


                ) {
                    if (response.isSuccessful && response.body() != null) {
                        println("Successful request to weather api Ambee Pollen")

                        val weather = response.body()!!
                        updatePollen(weather)
                        println(weather)
                    } else {
                        println("fail")
                    }
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MyLocationManager.REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                //denied
                val builder = AlertDialog.Builder(this)
                builder.setMessage(getString(R.string.need_permission))
                    .setTitle(getString(R.string.location_required))
                    .setIcon(R.drawable.info)
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        ) {
                            //only deny
                            MyLocationManager.checkLocationPermission(
                                this@HomeActivity,
                                this
                            )
                        } else {
                            //never ask again
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivityForResult(intent, MyLocationManager.REQUEST_CHECK_SETTINGS)
                        }
                    }
                    .setNegativeButton(getString(R.string.ask_me_later)) { _, _ ->
                        //call something that gets weather by city
                    }
                // Create the AlertDialog object and return it
                val dialog = builder.create()
                dialog.show()
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndRequestWeather()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MyLocationManager.REQUEST_CHECK_SETTINGS -> when (resultCode) {
                RESULT_OK -> {
                    Log.d("REQUEST_CHECK_SETTINGS", "RESULT_OK")
                    //case 1. You choose OK
                    Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()
                    getLocationAndRequestWeather()

                }
                RESULT_CANCELED -> {
                    Log.d("REQUEST_CHECK_SETTINGS", "RESULT_CANCELED")
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                    MyLocationManager.checkLocationPermission(
                        this@HomeActivity,
                        this
                    )
                }
                else -> {
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    //update the goals reading assigned exercises
    private fun updateGoals(
        goals: List<AssignedExercise>,
        completedExercisesThisWeek: List<CompletedExercise>?
    ) {
        val recyclerView: RecyclerView = findViewById(R.id.goalsRecyclerView)
        recyclerView.setHasFixedSize(true)

        val layoutManager =
            LinearLayoutManager(
                this@HomeActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
        recyclerView.layoutManager = layoutManager

        val adapter = GoalsAdapter(
            this@HomeActivity,
            goals, completedExercisesThisWeek
        )
        recyclerView.adapter = adapter
    }

    fun updateUserName(patient: Patient) {
        findViewById<TextView>(R.id.greeting).text =
            String.format(getString(R.string.greeting), patient.user?.firstName)
    }
    fun updateLastExercisesEmpty(){
        findViewById<CardView>(R.id.recentActivitiesView).visibility = View.GONE
        findViewById<TextView>(R.id.don_t_have_activities).visibility = View.VISIBLE
    }

    fun updateTheLastExercise(completedExercisesThisWeek: List<CompletedExercise>?) {
        if (completedExercisesThisWeek != null) {
            val lastExercise: CompletedExercise =
                completedExercisesThisWeek[completedExercisesThisWeek.size - 1]
            val icon = findViewById<ImageView>(R.id.recentActivityTypeIcon)
            lastExercise.exerciseType?.title?.let { getImageForActivityType(it) }?.let {
                icon.setImageResource(
                    it
                )
            }
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val startDateTimeParsed = parser.parse(lastExercise.startDateTime as String)
            val endDateTimeParsed = parser.parse(lastExercise.endDateTime as String)
            val formatterDate = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            val formattedDate = formatterDate.format(endDateTimeParsed as Date)
            val formatterTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedStartTime = formatterTime.format(startDateTimeParsed as Date)
            val formattedEndTime = formatterTime.format(endDateTimeParsed)
            val diff: Long =
                endDateTimeParsed.time - startDateTimeParsed.time
            val minutes = diff / 1000 / 60
            findViewById<TextView>(R.id.recentActivityDate).text = formattedDate
            findViewById<TextView>(R.id.recentActivityTimeSpan).text =
                String.format(getString(R.string.from_to), formattedStartTime, formattedEndTime)
            findViewById<TextView>(R.id.recentActivityDuration).text =
                String.format(getString(R.string.duration_minutes), minutes.toString())
            findViewById<TextView>(R.id.recentDistance).text = String.format(
                getString(R.string.distance_recent_home),
                lastExercise.distance?.toInt().toString()
            )
        } else {
            findViewById<CardView>(R.id.recentActivitiesView).visibility = View.GONE
        }
    }

    fun updateTemperatureHumidity(weather: WeatherForecast) {
        //save to cache
        weather.current?.humidity?.let { WeatherCache.setHumidity(it) }
        weather.current?.temp?.let { WeatherCache.setTemperature(it.roundToInt()) }
        weather.current?.weather?.get(0)?.icon.let {
            WeatherCache.setLinkToIcon(
                String.format(
                    getString(R.string.link_to_icon_weather),
                    weather.current?.weather?.get(0)?.icon
                )
            )
        }

        findViewById<TextView>(R.id.temperature).text = String.format(
            getString(R.string._temperature_c),
            weather.current?.temp?.roundToInt()
        )
        findViewById<ImageView>(R.id.imageWeather).load(
            String.format(
                getString(R.string.link_to_icon_weather),
                weather.current?.weather?.get(0)?.icon
            )
        )
        findViewById<TextView>(R.id.humidityPercentage).text = String.format(
            getString(R.string.humidity_percent),
            weather.current?.humidity
        )
        findViewById<ProgressBar>(R.id.progressBar1).progress = weather.current?.humidity as Int

    }


    fun updateAirQualityView(airQuality: Int) {
        //save to cache
        WeatherCache.setAirPollution(airQuality)
        findViewById<TextView>(R.id.airQualityIndex).text =
            airQuality.toString()
        val percentage = (100 * airQuality) / 5
        findViewById<ProgressBar>(R.id.progressBar3).progress = percentage
    }

    fun updatePollen(airPollen: AmbeePollen) {
        val statusPollen: String?
        if (airPollen.data[0].Risk.grass_pollen.contains("Very High") || airPollen.data[0].Risk.tree_pollen.contains(
                "Very High"
            ) || airPollen.data[0].Risk.weed_pollen.contains("Very High")
        ) {
            statusPollen = "Very High"
        } else if (airPollen.data[0].Risk.grass_pollen.contains("High") || airPollen.data[0].Risk.tree_pollen.contains(
                "High"
            ) || airPollen.data[0].Risk.weed_pollen.contains("High")
        ) {
            statusPollen = "High"
        } else if (airPollen.data[0].Risk.grass_pollen.contains("Medium") || airPollen.data[0].Risk.tree_pollen.contains(
                "Medium"
            ) || airPollen.data[0].Risk.weed_pollen.contains("Medium")
        ) {
            statusPollen = "Medium"
        } else {
            statusPollen = "Low"
        }
        WeatherCache.pollenRisk = statusPollen
        findViewById<TextView>(R.id.pollenIndex).text = statusPollen

    }

    override fun onLocationChanged(p0: Location) {
        print("Location changed")
    }

    //this one reads the test data from json file when the backend does not work
    fun readJson() {
        val userJSON: String?
        try {
            val inputStream: InputStream = assets.open("first.json")
            userJSON = inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            val patient = gson.fromJson(userJSON, Patient::class.java)
            updateUserName(patient)
            updateGoals(
                patient.assignedExercises as List<AssignedExercise>,
                patient.completedExercisesThisWeek
            )
        } catch (e: IOException) {
            println("can't read a file with a user")
        }
    }
}