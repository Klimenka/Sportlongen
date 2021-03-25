package nl.klimenko.sportlongen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import nl.klimenko.sportlongen.configuration.HelperConverter
import nl.klimenko.sportlongen.configuration.MyLocationManager
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.configuration.WeatherCache
import nl.klimenko.sportlongen.model.*
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class GoogleMapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {
    lateinit var exercise: Exercise
    private lateinit var sessionManager: SessionManager
    private lateinit var mMap: GoogleMap
    lateinit var mainHandler: Handler
    var duration: Int = 0
    private lateinit var startDate: String
    lateinit var endDate: String
    private lateinit var durationView: TextView
    private lateinit var durationViewShort: TextView
    var locationPoints: ArrayList<LocationPoint> = ArrayList()
    var oldLocation: Location? = null
    var totalDistance: Double = 0.0
    var maxPaceInSeconds: Int = 0
    var minPaceInSeconds: Int = 0
    private var userId: Int = -1
    var completedExercise: CompletedExerciseForPost? = null
    lateinit var loading: ProgressBar

    @RequiresApi(Build.VERSION_CODES.O)
    val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneId.of("Europe/Amsterdam"))

    private val updateTextTask = object : Runnable {
        override fun run() {
            plusOneSecond()
            mainHandler.postDelayed(this, 1000)
        }
    }

    fun plusOneSecond() {
        duration++
        val hours = duration / 3600
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60
        durationView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        durationViewShort.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun pauseTimer() {
        mainHandler.removeCallbacks(updateTextTask)
    }

    private fun resumeTimer() {
        mainHandler.post(updateTextTask)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_maps)

        sessionManager = SessionManager(this)
        loading = findViewById(R.id.loading_activity)
        startDate = dateTimeFormatter.format(Instant.now())
        userId = sessionManager.fetchUserId()
        durationView = findViewById(R.id.duration_counting)
        durationViewShort = findViewById(R.id.duration_counting_short)

        //start Listening updates with Location Manager
        MyLocationManager.checkLocationPermission(this@GoogleMapsActivity, this)

        exercise = intent.getSerializableExtra("Exercise") as Exercise

        //update views
        updateLimits(exercise)

        //update views
        findViewById<TextView>(R.id.activity_type).text = exercise.exerciseName
        findViewById<TextView>(R.id.activity_type_short).text = exercise.exerciseName
        //start timer on the screen
        mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(updateTextTask)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        loading.visibility = View.VISIBLE
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val whiteZoneLongView = findViewById<CardView>(R.id.white_zone)
        val whiteZoneShortView = findViewById<CardView>(R.id.white_zone_short)
        val asthmaButton = findViewById<ImageView>(R.id.asthma_button)
        val pauseButton = findViewById<ImageView>(R.id.pause_button)
        val continueButton = findViewById<ImageView>(R.id.continue_button)
        val stopButton = findViewById<ImageView>(R.id.stop_button)

        whiteZoneLongView.setOnClickListener{
            findViewById<CardView>(R.id.white_zone).visibility = View.INVISIBLE
            findViewById<CardView>(R.id.white_zone_short).visibility = View.VISIBLE
        }
        whiteZoneShortView.setOnClickListener{
            findViewById<CardView>(R.id.white_zone).visibility = View.VISIBLE
            findViewById<CardView>(R.id.white_zone_short).visibility = View.INVISIBLE
        }
        asthmaButton.setOnClickListener {
            endDate = dateTimeFormatter.format(Instant.now())
            intent = Intent(this, AsthmaAttackActivity::class.java)
            loading.visibility = View.VISIBLE
            createCompletedExerciseObject()
            intent.putExtra("completedExercise", completedExercise)
            startActivity(intent)
        }

        pauseButton.setOnClickListener {
            pauseButton.visibility = View.INVISIBLE
            findViewById<LinearLayout>(R.id.pauseState).visibility = View.VISIBLE
            pauseTimer()
        }
        continueButton.setOnClickListener {
            pauseButton.visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.pauseState).visibility = View.INVISIBLE
            resumeTimer()
        }
        stopButton.setOnClickListener {
            endDate = dateTimeFormatter.format(Instant.now())
            createCompletedExerciseObject()
            saveCompletedExercise()
            MyLocationManager.stopRequestLocationUpdates()
        }
    }

    private fun createCompletedExerciseObject() {
        completedExercise = CompletedExerciseForPost(
            ExerciseTypeList.getANumberInDBForType(exercise.exerciseName),
            userId,
            startDate,
            endDate,
            0,
            0,
            0,
            GpsDataPost(locationPoints),
            WeatherCache.myTemperature,
            WeatherCache.myHumidity,
            WeatherCache.myAirPollution,
            totalDistance,
            null,

            maxPaceInSeconds.toDouble(),
            minPaceInSeconds.toDouble(),
            ((maxPaceInSeconds + minPaceInSeconds) / 2).toDouble()
        )
    }

    //set the limits from the object that was made on the previous step (plan Activity)
    private fun updateLimits(exercise: Exercise) {
        if (exercise.durationInMinutes != null) {
            findViewById<TextView>(R.id.duration_limit).text = String.format(
                getString(R.string.maxLimits),
                HelperConverter.minutesIntToHoursMinutesSecondsString(exercise.durationInMinutes!!)
            )
        } else {
            findViewById<TextView>(R.id.duration_limit).visibility = View.INVISIBLE
        }
        if (exercise.targetDistance != null) {
            findViewById<TextView>(R.id.distance_limit).text =
                String.format(getString(R.string.maxLimits), exercise.targetDistance!!.toString())
        } else {
            findViewById<TextView>(R.id.distance_limit).visibility = View.INVISIBLE
        }
        if (exercise.maxPaceInSeconds != null) {
            findViewById<TextView>(R.id.pace_limit).text = String.format(
                getString(R.string.minLimits),
                HelperConverter.secondsToMinutesString(exercise.maxPaceInSeconds!!)
            )
        } else {
            findViewById<TextView>(R.id.pace_limit).visibility = View.INVISIBLE
        }
        if (exercise.maxHeartRate != null) {
            findViewById<TextView>(R.id.pulse_limit).text =
                String.format(getString(R.string.maxLimits), exercise.maxHeartRate!!.toString())
        } else {
            findViewById<TextView>(R.id.pulse_limit).visibility = View.INVISIBLE
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        loading.visibility = View.GONE
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.isMyLocationEnabled = true
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onLocationChanged(location: Location) {
        val zoomLevel = 16.0f
        locationPoints.add(
            LocationPoint(
                dateTimeFormatter.format(Instant.now()),
                location.latitude,
                location.longitude
            )
        )
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ), zoomLevel
            )
        )
        if (oldLocation != null) {
            //get a distance in km
            val distance = location.distanceTo(oldLocation) / 1000
            totalDistance += distance
            //update views
            findViewById<TextView>(R.id.distance_counting).text = "%.2f".format(totalDistance)
            findViewById<TextView>(R.id.distance_counting_short).text = "%.2f".format(totalDistance)

            oldLocation = location
            //because updates happens every 5 sec
            val pace = (5 / distance).toInt()
            updatePace(pace)
        } else {
            oldLocation = location
        }
        println(locationPoints)
    }

    private fun updatePace(pace: Int) {
        val paceString = HelperConverter.secondsToMinutesString(pace)
        findViewById<TextView>(R.id.pace_counting).text = paceString
        findViewById<TextView>(R.id.pace_counting_short).text = paceString
        if (maxPaceInSeconds < pace) {
            maxPaceInSeconds = pace
        } else if (minPaceInSeconds > pace) {
            minPaceInSeconds = pace
        }
    }

    private fun saveCompletedExercise() {
        loading.visibility = View.VISIBLE
        this.completedExercise?.let {
            RetrofitFactoryBackend.getBackendServiceObject()
                .postCompletedExercise(sessionManager.fetchAuthToken(), it)
                .enqueue(object : Callback<CompletedExercise> {
                    override fun onFailure(call: Call<CompletedExercise>, t: Throwable) {
                        loading.visibility = View.GONE
                        //TODO to save the completed exercise to the internal DB with the the flag isNotUploaded
                        println("failure to save the completed exercise")
                    }

                    override fun onResponse(
                        call: Call<CompletedExercise>, response: Response<CompletedExercise>
                    ) {
                        loading.visibility = View.GONE
                        if (response.isSuccessful && response.body() != null) {
                            println("got an object of a completed exercise")
                            val savedCompletedExercise = response.body()!!
                            intent =
                                Intent(this@GoogleMapsActivity, QuestionnaireActivity::class.java)
                            intent.putExtra("CompletedExercise", savedCompletedExercise)
                            startActivity(intent)

                        } else {
                            //TODO to save the completed exercise to the internal DB with the the flag isNotUploaded
                            println("got an empty response from get a patient call")
                            val intent1 = Intent(this@GoogleMapsActivity, LoginActivity::class.java)
                            println("unauthorized")
                            startActivity(intent1)
                        }
                    }
                })
        }
    }
}