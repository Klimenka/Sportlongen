package nl.klimenko.sportlongen.configuration

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import java.lang.ref.WeakReference


object MyLocationManager {
    const val REQUEST_CODE = 123
    lateinit var mActivity: Activity
    lateinit var myLocationListener: LocationListener

    fun checkLocationPermission(
        activity: Activity,
        locationListener: LocationListener
    ) {
        mActivity = activity
        myLocationListener = locationListener
        if (ContextCompat.checkSelfPermission(
                mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                mActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
        } else {
            Thread(Runnable {
                // Moves the current Thread into the background
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND) //
                requestLocationUpdates(mActivity)
            }).start()
        }
    }

    /**
     * be used in HomeActivity.
     */
    const val REQUEST_CHECK_SETTINGS = 55

    /**
     * The number of millis in the future from the call to start().
     * until the countdown is done and onFinish() is called.
     *
     *
     * It is also the interval along the way to receive onTick(long) callbacks.
     */
    private const val TWENTY_SECS: Long = 20000

    /**
     * Timer to get location from history when requestLocationUpdates don't return result.
     */
    private var mCountDownTimer: CountDownTimer? = null

    /**
     * WeakReference of current activity.
     */
    private var mWeakReferenceActivity: WeakReference<Activity>? = null

    @Synchronized
    fun requestLocationUpdates(activity: Activity) {
        mWeakReferenceActivity = if (mWeakReferenceActivity == null) {
            WeakReference(activity)
        } else {
            mWeakReferenceActivity?.clear()
            WeakReference(activity)
        }
        //create location request: https://developer.android.com/training/location/change-location-settings.html#prompt
        val mLocationRequest = LocationRequest()
        // Which your app prefers to receive location updates. Note that the location updates may be
        // faster than this rate, or slower than this rate, or there may be no updates at all
        // (if the device has no connectivity)
        mLocationRequest.interval = 10000
        //This method sets the fastest rate in milliseconds at which your app can handle location updates.
        // You need to set this rate because other apps also affect the rate at which updates are sent
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //Get Current Location Settings
        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        //Next check whether the current location settings are satisfied
        val client = LocationServices.getSettingsClient(activity)
        val task = client.checkLocationSettings(builder.build())
        //Prompt the User to Change Location Settings
        task.addOnSuccessListener(activity) {
            Log.d("CurrentLocationManager", "OnSuccessListener")
            // All location settings are satisfied. The client can initialize location requests here.
            // If it's failed, the result after user updated setting is sent to onActivityResult of HomeActivity.
            val activity1 = mWeakReferenceActivity?.get()
            if (activity1 != null) {
                startRequestLocationUpdate(activity1.applicationContext)
            }
        }

        task.addOnFailureListener(activity) { e ->
            Log.d("CurrentLocationManager", "addOnFailureListener")
            val statusCode = (e as ApiException).statusCode
            when (statusCode) {
                CommonStatusCodes.RESOLUTION_REQUIRED ->
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        val activity1 = mWeakReferenceActivity?.get()
                        if (activity1 != null) {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            val resolvable = e as ResolvableApiException
                            resolvable.startResolutionForResult(
                                activity1, REQUEST_CHECK_SETTINGS
                            )
                        }
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                        sendEx.printStackTrace()
                    }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    // Location settings are not satisfied. However, we have no way
                    // to fix the settings so we won't show the dialog.
                }
            }
        }
    }

    fun startRequestLocationUpdate(appContext: Context) {
        val mLocationManager =
            appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                appContext.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 0f, myLocationListener

                )
            } else {
                mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 5000, 0f, myLocationListener
                )
            }
        }
        countDownUpdateLocation()
    }

    fun getLocationOnce(activity: Activity): Location? {
        var loc: Location? = null
        val mLocationManager =
            activity.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                activity.applicationContext.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                Log.d("GPS", "GPS")
                if (loc != null) {
                    Log.d(
                        TAG,
                        "getCurrentLocationNetwork: " + loc.latitude + ", " + loc.longitude
                    )
                    return loc

                } else {
                    Log.d("Network", "Network")
                    loc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (loc != null) {
                        Log.d(
                            TAG,
                            "getCurrentLocationNetwork: " + loc.latitude + ", " + loc.longitude
                        )
                        return loc
                    }
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
        }
        return loc
    }

    @Synchronized
    private fun countDownUpdateLocation() {
        mCountDownTimer?.cancel()
        mCountDownTimer = object : CountDownTimer(TWENTY_SECS, TWENTY_SECS) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                if (mWeakReferenceActivity != null) {
                    val activity = mWeakReferenceActivity?.get()
                    if (activity != null && ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val location = (activity.applicationContext
                            .getSystemService(Context.LOCATION_SERVICE) as LocationManager)
                            .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                        location?.let { myLocationListener.onLocationChanged(it) }
                    } else {
                        stopRequestLocationUpdates()
                    }
                } else {
                    mCountDownTimer?.cancel()
                    mCountDownTimer = null
                }
            }
        }.start()
    }

    fun stopRequestLocationUpdates() {
        val activity = mWeakReferenceActivity?.get()
        if (activity != null) {
            (activity.applicationContext
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager).removeUpdates(
                myLocationListener
            )
        }
        mCountDownTimer?.cancel()
        mCountDownTimer = null
    }


}

