package nl.klimenko.sportlongen.configuration

import nl.klimenko.sportlongen.R

object ImageActivity {
    /*
    Returns resource id to set to setImageResource
     */
    fun getImageForActivityType(activityType : String) : Int{
        when (activityType) {
            "Cycling" -> return R.drawable.cycling_image
            "Running" -> return R.drawable.running_image
            "Hiking" -> return R.drawable.hiking_image
            "Walking" -> return R.drawable.walking_image
            "Swimming" -> return R.drawable.swimming_image

        }
        return R.drawable.default_image
    }
}