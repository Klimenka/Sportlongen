package nl.klimenko.sportlongen

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.util.forEach
import com.google.android.material.slider.Slider
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.model.CompletedExercise
import nl.klimenko.sportlongen.model.ExerciseLog
import nl.klimenko.sportlongen.model.SymptomsList
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.format.DateTimeFormatter


class QuestionnaireActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    var isClicked = false
    var savedChoices = ArrayList<Boolean>()
    var howFeel = 0
    var isAsthma = 0
    var isHayFever = 0
    var isLookingForward = 0
    var openReason: String? = ""
    var dateTime = ""
    var exerciseLog: ExerciseLog? = null
    lateinit var completedExercise: CompletedExercise

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        savedChoices.add(false)
        savedChoices.add(false)
        savedChoices.add(false)
        savedChoices.add(false)
        completedExercise = intent.getSerializableExtra("CompletedExercise") as CompletedExercise
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire)
        sessionManager = SessionManager(this)
        findViewById<ImageView>(R.id.confirm_button).setOnClickListener {
            howFeel = findViewById<Slider>(R.id.slider1).value.toInt()
            isAsthma = findViewById<Slider>(R.id.slider2).value.toInt()
            isHayFever = findViewById<Slider>(R.id.slider3).value.toInt()
            dateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            for (i in savedChoices.indices) {
                if (savedChoices[i]) {
                    openReason = openReason + SymptomsList.symptomsList[i] + " "
                }
            }
            val openReasonView = findViewById<EditText>(R.id.open_reason)
            if (openReasonView.text.toString() != "") {
                openReason = openReason + openReasonView.text.toString() + " "
            }
            if (openReason == "") {
                openReason = "none"
            }
            exerciseLog = completedExercise.completedExerciseId?.let { it1 ->
                ExerciseLog(
                    it1, howFeel, isAsthma, isHayFever, isLookingForward, openReason, 0, dateTime
                )
            }
            saveToDBAndOpenSummary(exerciseLog!!)
        }

        val listView = findViewById<ListView>(R.id.list_item)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        findViewById<LinearLayout>(R.id.choose_symptoms).setOnClickListener {
            isClicked = !isClicked
            if (isClicked) {
                listView.visibility = View.VISIBLE
                findViewById<ImageView>(R.id.drop_down_symptoms).visibility = View.GONE
                findViewById<ImageView>(R.id.drop_up_symptoms).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.confirm_button).visibility = View.INVISIBLE
                findViewById<CardView>(R.id.commentCard).visibility = View.INVISIBLE

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_multiple_choice, SymptomsList.symptomsList
                )
                listView.setAdapter(adapter)

                val cntChoice = listView.getCount()

                for (i in 0 until cntChoice) {
                    listView.setItemChecked(i, savedChoices[i])
                }

            } else {
                listView.getCheckedItemPositions().forEach { index: Int, value: Boolean ->
                    savedChoices.set(index, value)
                }
                listView.visibility = View.GONE
                findViewById<ImageView>(R.id.drop_down_symptoms).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.drop_up_symptoms).visibility = View.GONE
                findViewById<CardView>(R.id.commentCard).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.confirm_button).visibility = View.VISIBLE
            }
        }

    }

    fun saveToDBAndOpenSummary(exerciseLog: ExerciseLog) {
        RetrofitFactoryBackend.getBackendServiceObject()
            .postExerciseLog(sessionManager.fetchAuthToken(), exerciseLog)
            .enqueue(object : Callback<ExerciseLog> {
                override fun onFailure(call: Call<ExerciseLog>, t: Throwable) {
                    //maybe got something from the save state
                    println("failure to save the exercise log")
                }

                override fun onResponse(
                    call: Call<ExerciseLog>, response: Response<ExerciseLog>
                ) {
                    if (response.isSuccessful && response.body() != null) {

                        println("got an object of an exercise log")
                        intent = Intent(this@QuestionnaireActivity, SummaryActivity::class.java)
                        intent.putExtra("CompletedExercise", completedExercise)
                        startActivity(intent)
                    } else {
                        println(response.message())
                        println("got an empty response from get a patient call")
                    }
                }
            })

    }
}


