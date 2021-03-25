package nl.klimenko.sportlongen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import nl.klimenko.sportlongen.adapters.NotificationAdapter
import nl.klimenko.sportlongen.configuration.SessionManager
import nl.klimenko.sportlongen.model.CompletedExercise

import nl.klimenko.sportlongen.model.Notification
import nl.klimenko.sportlongen.service.RetrofitFactoryBackend
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NotificationActivity : BaseActivity() {
    lateinit var sessionManager : SessionManager
    lateinit var loading: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        loading = findViewById(R.id.loading)
        getNotifications()

    }
    fun  getNotifications(){
        loading.visibility = View.VISIBLE
        RetrofitFactoryBackend.getBackendServiceObject()
            .getNotifications(sessionManager.fetchAuthToken(), sessionManager.fetchUserId())
            .enqueue(object : Callback<List<Notification>> {
                override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                    println("failure to get notifications")
                    loading.visibility = View.GONE
                    findViewById<RecyclerView>(R.id.notificationsRecyclerView).visibility = View.GONE
                    findViewById<TextView>(R.id.error_occurred).visibility = View.VISIBLE
                    val buttonTryAgain = findViewById<Button>(R.id.try_again)
                    buttonTryAgain.visibility = View.VISIBLE
                    buttonTryAgain.setOnClickListener{
                        getNotifications()
                    }
                }

                override fun onResponse(
                    call: Call<List<Notification>>, response: Response<List<Notification>>
                ) {
                    when {
                        response.code() == 200 -> {
                            loading.visibility = View.GONE
                            println("got notifications")
                            val notifications = response.body()!!
                            if (notifications.isNotEmpty()) {
                                updateNotifications(notifications)
                            } else{
                                updateNotificationsEmpty()
                            }
                        }
                        response.code() == 401 -> {
                            loading.visibility = View.GONE
                            val intent1 = Intent(this@NotificationActivity, LoginActivity::class.java)
                            println("unauthorized")
                            startActivity(intent1)
                        }
                        else -> {
                            loading.visibility = View.GONE
                            findViewById<RecyclerView>(R.id.notificationsRecyclerView).visibility = View.GONE
                            findViewById<TextView>(R.id.error_occurred).visibility = View.VISIBLE
                            val buttonTryAgain = findViewById<Button>(R.id.try_again)
                            buttonTryAgain.visibility = View.VISIBLE
                            buttonTryAgain.setOnClickListener{
                                getNotifications()
                            }
                        }
                    }
                }
            })
    }
    private fun updateNotifications(notifications: List<Notification>) {
        val recyclerView: RecyclerView = findViewById(R.id.notificationsRecyclerView)
        recyclerView.setHasFixedSize(true)

        val layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        recyclerView.layoutManager = layoutManager

        val adapter = NotificationAdapter(
            this, notifications
        )
        recyclerView.adapter = adapter
    }
    private fun updateNotificationsEmpty(){
        findViewById<RecyclerView>(R.id.notificationsRecyclerView).visibility = View.GONE
        findViewById<TextView>(R.id.don_t_have_activities).visibility = View.VISIBLE
    }
    override fun getContentViewId(): Int {
        return R.layout.activity_notification
    }

    override fun getNavigationMenuItemId(): Int {
        return R.id.notificationsMenuButton
    }
}