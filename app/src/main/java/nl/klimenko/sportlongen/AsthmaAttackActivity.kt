package nl.klimenko.sportlongen

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.model.CompletedExercise
import nl.klimenko.sportlongen.model.CompletedExerciseForPost
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AsthmaAttackActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var completedExercise: CompletedExerciseForPost
    private lateinit var loading: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asthma_attack)

        loading = findViewById(R.id.loading_register)
        sessionManager = SessionManager(this)
        completedExercise =
            intent.getSerializableExtra("completedExercise") as CompletedExerciseForPost

        findViewById<ImageView>(R.id.continueAsthmaButton).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.want_to_continue))

            builder.setPositiveButton(getString(R.string.wait_for_better)) { _, _ ->
                this.completedExercise.let {
                    loading.visibility = View.VISIBLE
                    RetrofitFactoryBackend.getBackendServiceObject()
                        .postCompletedExercise(sessionManager.fetchAuthToken(), it)
                        .enqueue(object : Callback<CompletedExercise> {
                            override fun onFailure(call: Call<CompletedExercise>, t: Throwable) {
                                /* TODO save to internal DB and set the flag isUploaded to false */
                                loading.visibility = View.GONE
                                println("failure to save the completed exercise")
                            }

                            override fun onResponse(
                                call: Call<CompletedExercise>, response: Response<CompletedExercise>
                            ) {
                                loading.visibility = View.GONE
                                if (response.isSuccessful && response.body() != null) {
                                    println("got an object of a completed exercise")
                                    val savedCompletedExercise = response.body()!!
                                    intent = Intent(
                                        this@AsthmaAttackActivity,
                                        QuestionnaireActivity::class.java
                                    )
                                    intent.putExtra("CompletedExercise", savedCompletedExercise)
                                    startActivity(intent)

                                } else {
                                    //TODO save to internal DB and set the flag isUploaded to false
                                    val intent1 = Intent(this@AsthmaAttackActivity, LoginActivity::class.java)
                                    println("unauthorized")
                                    startActivity(intent1)
                                }
                            }
                        })
                }
            }

            builder.setNegativeButton(getString(R.string.i_ll_continue)) { _, _ ->
                //TODO solution for now is to send the user back, but later it needs to get the timestamp and save it to DB
                this.finish()
            }

            builder.show()
        }
    }
}