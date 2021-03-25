package nl.klimenko.sportlongen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import coil.load
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import nl.klimenko.sportlongen.configuration.HelperConverter
import nl.klimenko.sportlongen.configuration.ImageActivity
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.configuration.WeatherCache
import nl.klimenko.sportlongen.model.CompletedExercise
import nl.klimenko.sportlongen.model.ExerciseTypeList
import nl.klimenko.sportlongen.model.LocationPoint
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.String.format
import java.text.SimpleDateFormat
import java.util.*


class SummaryActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var completedExercise: CompletedExercise
    private lateinit var sessionManager: SessionManager
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val formatterDate = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
    val formatterTime = SimpleDateFormat("hh:mm aaa", Locale.getDefault())
    private lateinit var mMap: GoogleMap
    lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)
        loading = findViewById(R.id.loading)
        loading.visibility = View.VISIBLE
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sessionManager = SessionManager(this)
        completedExercise = intent.getSerializableExtra("CompletedExercise") as CompletedExercise
        setLayout()
        findViewById<ImageView>(R.id.summary_close).setOnClickListener {
            intent = Intent(this@SummaryActivity, HomeActivity::class.java)
            startActivity(intent)
        }
        findViewById<ImageView>(R.id.summaryDeleteIcon).setOnClickListener {
            RetrofitFactoryBackend.getBackendServiceObject()
                .deleteCompletedExercise(
                    sessionManager.fetchAuthToken(),
                    completedExercise.completedExerciseId!!
                )
                .enqueue(object : Callback<Void> {
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@SummaryActivity, this@SummaryActivity.getString(R.string.something_went_wrong_please_try_again), Toast.LENGTH_LONG).show()
                        println("failure to delete a completed exercise")
                    }

                    override fun onResponse(
                        call: Call<Void>, response: Response<Void>
                    ) {
                        if (response.isSuccessful) {
                            println("the exercise has been deleted")
                            println(response.body())
                            Toast.makeText(
                                this@SummaryActivity,
                                "Your exercise has been deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                            intent = Intent(this@SummaryActivity, HomeActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@SummaryActivity, this@SummaryActivity.getString(R.string.something_went_wrong_please_try_again), Toast.LENGTH_LONG).show()
                            println(response.message())
                            println("got an empty response from delete an exercise")
                        }
                    }
                })
        }
    }

    private fun setLayout() {

        println(completedExercise)
        val icon = findViewById<ImageView>(R.id.summaryActivityTypeIcon)

        //this one should be used. It does not work because the backend returns null object for ExerciseType
        //val exerciseType = completedExercise.exerciseType?.title
        val exerciseType = completedExercise.exerciseTypeId?.let { ExerciseTypeList.getATypeByANumber(it) }
        exerciseType?.let { ImageActivity.getImageForActivityType(it) }?.let {
            icon.setImageResource(
                it
            )
        }

        findViewById<TextView>(R.id.summaryActivityTypeText).text =
            completedExercise.exerciseTypeId?.let { ExerciseTypeList.getATypeByANumber(it) }

        val parsedEndDate = parser.parse(completedExercise.endDateTime as String)
        val parsedStartDate = parser.parse(completedExercise.startDateTime as String)
        val dateForSummary =
            formatterDate.format(parsedEndDate as Date) + " " + formatterTime.format(parsedEndDate as Date)
        val diff: Long =
            parsedEndDate.time - parsedStartDate.time
        val minutes = diff / 1000 / 60
        findViewById<TextView>(R.id.summaryDate).text = dateForSummary
        findViewById<TextView>(R.id.summaryDistance).text =
            format("%.2f", completedExercise.distance?.toDouble())
        findViewById<TextView>(R.id.summaryDuration).text =
            HelperConverter.minutesIntToHoursMinutesSecondsString(minutes.toInt())
        findViewById<TextView>(R.id.summaryAvgPace).text = completedExercise.avgPace?.toInt()?.let {
            HelperConverter.secondsToMinutesString(
                it
            )
        }
        findViewById<TextView>(R.id.summaryMaxPace).text = completedExercise.minPace?.toInt()?.let {
            HelperConverter.secondsToMinutesString(
                it
            )
        }
        /*   findViewById<TextView>(R.id.summaryAvgHeartRate).text =
               completedExercise.avgHeartRate.toString()
           findViewById<TextView>(R.id.summaryMaxHeartRate).text =
               completedExercise.maxHeartRate.toString()


         */
        //weather data
        findViewById<TextView>(R.id.summaryPollenRisk).text = WeatherCache.pollenRisk
        findViewById<TextView>(R.id.summaryHumidity).text = WeatherCache.myHumidity.toString()
        findViewById<TextView>(R.id.summaryAirQuality).text = WeatherCache.myAirPollution.toString()
        findViewById<TextView>(R.id.summaryTemperature).text =
            format(getString(R.string._temperature_c), WeatherCache.myTemperature.toString())
        findViewById<ImageView>(R.id.summaryTemperatureIcon).load(WeatherCache.getLinkToIcon())
        findViewById<TextView>(R.id.summaryFeedback).text = completedExercise.feedback?.message
        loading.visibility = View.GONE
    }
    @SuppressLint("MissingPermission")
    private fun showPolyline() {
        val listOfPoints: MutableList<LatLng> = mutableListOf<LatLng>()
        if(completedExercise.gpsData !=null && completedExercise.gpsData?.locationPoints !=null ) {
            for (location: LocationPoint in completedExercise.gpsData?.locationPoints!!) {
                location.latitude?.let { location.longtitude?.let { it1 -> LatLng(it, it1) } }
                    ?.let { listOfPoints.add(it) }
            }

            mMap.addPolyline(
                PolylineOptions()
                    .addAll(
                        listOfPoints
                    )
                    .color(Color.RED)
                    .width(10f)
            )
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    listOfPoints[0],
                    13f
                )
            )
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
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
        } else{
            showPolyline()
        }

    }
}