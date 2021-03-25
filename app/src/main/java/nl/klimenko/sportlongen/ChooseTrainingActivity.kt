package nl.klimenko.sportlongen

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.klimenko.sportlongen.adapters.ExerciseTypesAdapter
import nl.klimenko.sportlongen.configuration.HelperConverter
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.model.AssignedExercise
import nl.klimenko.sportlongen.model.Exercise
import nl.klimenko.sportlongen.model.ExerciseTypeList
import nl.klimenko.sportlongen.model.Patient
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

interface ExerciseListener {
    fun onExerciseClicked(exercise: Exercise)
}

class ChooseTrainingActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    lateinit var myDialog: Dialog
    lateinit var patient: Patient
    lateinit var exerciseToActivity: Exercise

    lateinit var finalTitleExercise: String
    lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_training)
        //default one if no exercises recommended and user did not click to any exercise type.
        finalTitleExercise = getString(R.string.walking)
        loading = findViewById(R.id.loading_planning)
        patient = intent.getSerializableExtra("Patient") as Patient

        val listOfExercisesWithLimits =
            addEmptyObjectsToAssignedExercises(patient.assignedExercises)
        if (listOfExercisesWithLimits[0].durationInMinutes != null) {
            finalTitleExercise = listOfExercisesWithLimits[0].exerciseName
            updateRecommendedFields(listOfExercisesWithLimits[0])
        }

        val recyclerView: RecyclerView = findViewById(R.id.plan_activity_recycler_view)
        val layoutManager =
            LinearLayoutManager(
                this@ChooseTrainingActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        recyclerView.layoutManager = layoutManager
        val adapter = ExerciseTypesAdapter(
            this@ChooseTrainingActivity,
            listOfExercisesWithLimits,
            object : ExerciseListener {
                override fun onExerciseClicked(exercise: Exercise) {
                    finalTitleExercise = exercise.exerciseName
                    if (exercise.durationInMinutes != null) {
                        updateRecommendedFields(exercise)
                    } else {
                        findViewById<TextView>(R.id.plan_activity_duration_rec).text = ""
                        findViewById<TextView>(R.id.plan_activity_distance_rec).text = ""
                        findViewById<TextView>(R.id.plan_activity_pace_rec).text = ""
                        findViewById<TextView>(R.id.plan_activity_heart_rate_rec).text = ""

                    }
                }
            }
        )
        recyclerView.adapter = adapter

        myDialog = Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
        sessionManager = SessionManager(this)
        //test for dialog windows (next 2 lines) should be uncommented
       //  sessionManager.saveUserChoiceNotificationNeverAsk(false)
        // sessionManager.saveUserChoiceInhalerNotificationNeverAsk(false)

        findViewById<ImageView>(R.id.plan_activity_start_button).setOnClickListener {
            val duration: Int? =
                if (findViewById<EditText>(R.id.plan_activity_duration_rec_set).text.toString() != "") {
                    findViewById<EditText>(R.id.plan_activity_duration_rec_set).text.toString()
                        .toInt()
                } else {
                    null
                }
            val distance: Double? =
                if (findViewById<EditText>(R.id.plan_activity_distance_rec_set).text.toString() != ""
                ) {
                    findViewById<EditText>(R.id.plan_activity_distance_rec_set).text.toString()
                        .toDouble()
                } else {
                    null
                }
            val pace: Int? =
                if (findViewById<EditText>(R.id.plan_activity_pace_rec_set).text.toString() != ""
                ) {
                    HelperConverter.minutesStringToSeconds(findViewById<EditText>(R.id.plan_activity_pace_rec_set).text.toString())
                } else {
                    null
                }
            val heartRate: Int? =
                if (findViewById<EditText>(R.id.plan_activity_heart_rate_rec_set).text.toString() != ""
                ) {
                    findViewById<EditText>(R.id.plan_activity_heart_rate_rec_set).text.toString()
                        .toInt()
                } else {
                    null
                }

            exerciseToActivity = Exercise(finalTitleExercise, distance, duration, pace, heartRate)
            intent = Intent(this, GoogleMapsActivity::class.java)
            intent.putExtra("Exercise", exerciseToActivity)

            if (!sessionManager.checkIfNotShowNotifications()) {
                myDialog.let {
                    RemindersView().openRemindersView(
                        it,
                        sessionManager,
                        this@ChooseTrainingActivity,
                        intent
                    )
                }
            } else if (!sessionManager.checkIfNotShowInhalerNotifications()) {
                myDialog.let {
                    InhalerReminder().openRemindersView(
                        it,
                        sessionManager,
                        this@ChooseTrainingActivity,
                        intent
                    )
                }
            } else {
                loading.visibility = View.VISIBLE
                startActivity(intent)
            }
        }
        findViewById<ImageView>(R.id.plan_activity_set_button).setOnClickListener {
            updateSetRecommendedFields()
        }
        findViewById<ImageView>(R.id.plan_activity_close).setOnClickListener {
            this.finish()
        }
    }

    /*
    This method prepares list of exercises for the adapter with exercises, where the first exercises have limits from a care provider
     */
    private fun addEmptyObjectsToAssignedExercises(assignedExercisesImmutable: List<AssignedExercise>?): ArrayList<Exercise> {

        val allExercisesTypes = ExerciseTypeList.typesOfExercisesList
        var minutes: Int?
        val assignedExercises: ArrayList<Exercise> = ArrayList()
        val parser = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        if (assignedExercisesImmutable != null) {
            //go through all assigned exercises and remove their names from list of all types of exercises
            for (assignedExercise: AssignedExercise in assignedExercisesImmutable) {
                if (allExercisesTypes.contains(assignedExercise.exerciseType.title)) {
                    allExercisesTypes.remove(assignedExercise.exerciseType.title)
                }
                val parsedTime = parser.parse(assignedExercise.maxDuration)
                minutes = if (parsedTime != null) {
                    parsedTime.time.toInt() / 1000 / 60
                } else {
                    null
                }
                val exercise = Exercise(
                    assignedExercise.exerciseType.title,
                    assignedExercise.maxDistance,
                    minutes,
                    assignedExercise.maxPace,
                    assignedExercise.maxHeartRate
                )
                assignedExercises.add(exercise)
            }
        }
        //create a new exercise with nulls except the type of the exercise and add them to list of assigned exercises
        for (s: String in allExercisesTypes) {
            val exercise = Exercise(s, null, null, null, null)

            assignedExercises.add(exercise)
        }
        return assignedExercises
    }

    fun updateRecommendedFields(exercise: Exercise) {
        findViewById<TextView>(R.id.plan_activity_duration_rec).text =
            String.format(
                getString(R.string.duration_recommended),
                exercise.durationInMinutes.toString()
            )
        findViewById<TextView>(R.id.plan_activity_distance_rec).text = String.format(
            getString(R.string.distance_recommended),
            exercise.targetDistance.toString()
        )
        if (exercise.maxPaceInSeconds != null) {
            findViewById<TextView>(R.id.plan_activity_pace_rec).text =
                String.format(
                    getString(R.string.pace_recommended),
                    HelperConverter.secondsToMinutesString(exercise.maxPaceInSeconds!!)
                )
        }
        findViewById<TextView>(R.id.plan_activity_heart_rate_rec).text = String.format(
            getString(R.string.heart_rate_recommended),
            exercise.maxHeartRate.toString()
        )
    }

    private fun updateSetRecommendedFields() {

        findViewById<EditText>(R.id.plan_activity_duration_rec_set).setText(
            findViewById<TextView>(R.id.plan_activity_duration_rec).text.split(
                " "
            )[0]
        )
        findViewById<EditText>(R.id.plan_activity_distance_rec_set).setText(
            findViewById<TextView>(R.id.plan_activity_distance_rec).text.split(
                " "
            )[0]
        )
        findViewById<EditText>(R.id.plan_activity_pace_rec_set).setText(
            findViewById<TextView>(R.id.plan_activity_pace_rec).text.split(
                " "
            )[0]
        )
        findViewById<EditText>(R.id.plan_activity_heart_rate_rec_set).setText(
            findViewById<TextView>(
                R.id.plan_activity_heart_rate_rec
            ).text.split(" ")[0]
        )
    }


}