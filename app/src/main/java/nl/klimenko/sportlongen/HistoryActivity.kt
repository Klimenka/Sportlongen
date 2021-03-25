package nl.klimenko.sportlongen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_history.*
import nl.klimenko.sportlongen.adapters.HistoryAdapter
import nl.klimenko.sportlongen.adapters.HistoryListener
import nl.klimenko.sportlongen.configuration.PatientCache
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.model.AssignedExercise
import nl.klimenko.sportlongen.model.CompletedExercise
import nl.klimenko.sportlongen.model.Patient
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HistoryActivity : BaseActivity() {
    lateinit var sessionManager: SessionManager
    lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        loading = findViewById(R.id.loading_history)
        getHistoryActivities()
    }
    override fun onResume() {
        super.onResume()
        sessionManager = SessionManager(this)
        loading = findViewById(R.id.loading_history)
        getHistoryActivities()
    }

    private fun updateHistory(completedExercisesThisWeek: List<CompletedExercise>) {
        val recyclerView: RecyclerView = findViewById(R.id.historyRecyclerView)
        recyclerView.setHasFixedSize(true)

        val layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        recyclerView.layoutManager = layoutManager

        val adapter = HistoryAdapter(
            this, completedExercisesThisWeek,
            object : HistoryListener {
                override fun onHistoryClicked(exerciseId: Int) {
                    println(exerciseId)
                    getCompletedExercise(exerciseId)
                }
            }
        )
        recyclerView.adapter = adapter
    }

    private fun updateHistoryEmpty() {
        findViewById<RecyclerView>(R.id.historyRecyclerView).visibility = View.GONE
        findViewById<TextView>(R.id.don_t_have_activities).visibility = View.VISIBLE
    }


    private fun getHistoryActivities() {
        loading.visibility = View.VISIBLE

        RetrofitFactoryBackend.getBackendServiceObject()
            .getCompletedExercises(sessionManager.fetchAuthToken(), sessionManager.fetchUserId())
            .enqueue(object : Callback<List<CompletedExercise>> {
                override fun onFailure(call: Call<List<CompletedExercise>>, t: Throwable) {
                    findViewById<ScrollView>(R.id.scrollView).visibility = View.VISIBLE
                    println("failure to get history")
                    loading.visibility = View.GONE
                    findViewById<RecyclerView>(R.id.historyRecyclerView).visibility = View.GONE
                    findViewById<TextView>(R.id.error_occurred).visibility = View.VISIBLE
                    val buttonTryAgain = findViewById<Button>(R.id.try_again)
                    buttonTryAgain.visibility = View.VISIBLE
                    buttonTryAgain.setOnClickListener {
                        getHistoryActivities()
                    }
                }

                override fun onResponse(
                    call: Call<List<CompletedExercise>>, response: Response<List<CompletedExercise>>
                ) {
                    findViewById<ScrollView>(R.id.scrollView).visibility = View.VISIBLE
                    when {
                        response.code() == 200 -> {
                            loading.visibility = View.GONE
                            println("got an object of a user")
                            val completedExercises = response.body()!!
                            if (completedExercises.isNotEmpty()) {
                                updateHistory(completedExercises.asReversed())
                            } else {
                                updateHistoryEmpty()
                            }
                        }
                        response.code() == 401 -> {
                            loading.visibility = View.GONE
                            val intent1 = Intent(this@HistoryActivity, LoginActivity::class.java)
                            println("unauthorized")
                            startActivity(intent1)
                        }
                        else -> {
                            loading.visibility = View.GONE
                            findViewById<RecyclerView>(R.id.historyRecyclerView).visibility =
                                View.GONE
                            findViewById<TextView>(R.id.error_occurred).visibility = View.VISIBLE
                            val buttonTryAgain = findViewById<Button>(R.id.try_again)
                            buttonTryAgain.visibility = View.VISIBLE
                            buttonTryAgain.setOnClickListener {
                                getHistoryActivities()
                            }
                        }
                    }
                }
            })
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_history
    }

    override fun getNavigationMenuItemId(): Int {
        return R.id.historyMenuButton
    }

    fun getCompletedExercise(id: Int) {
        loading.visibility = View.VISIBLE
        findViewById<ScrollView>(R.id.scrollView).visibility = View.GONE
        RetrofitFactoryBackend.getBackendServiceObject()
            .getCompletedExercise(sessionManager.fetchAuthToken(), id)
            .enqueue(object : Callback<CompletedExercise> {
                override fun onFailure(call: Call<CompletedExercise>, t: Throwable) {
                    //maybe got something from the save state
                    println("failure to get a completed exercise")
                    loading.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<CompletedExercise>, response: Response<CompletedExercise>
                ) {
                    when {
                        response.code() == 200 -> {
                            println("got an object of a user")
                            val completedExercise = response.body()!!
                            println(completedExercise)
                            intent = Intent(this@HistoryActivity, SummaryActivity::class.java)
                            intent.putExtra("CompletedExercise", completedExercise)
                            startActivity(intent)

                        }
                        response.code() == 401 -> {
                            loading.visibility = View.GONE
                            val intent1 = Intent(this@HistoryActivity, LoginActivity::class.java)
                            println("unauthorized")
                            startActivity(intent1)
                        }
                        else -> {
                            loading.visibility = View.GONE
                            Toast.makeText(
                                this@HistoryActivity,
                                this@HistoryActivity.getString(R.string.a_network_error_occurred_please_try_again),
                                Toast.LENGTH_LONG
                            ).show()
                            println("got an empty response from get a completed exercise call")
                        }
                    }
                }
            })
    }
}