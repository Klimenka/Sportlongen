package nl.klimenko.sportlongen

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import nl.klimenko.sportlongen.configuration.PatientCache

/*
This activity gives the option to use the same menu bar on other activities
To use it, it needs to be implemented and setContentView (R.layout.) should be removed from the activity
The method getContentViewId will set it properly
 */
abstract class BaseActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var menuBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentViewId())

        menuBar = findViewById(R.id.appBar)
        menuBar.setOnNavigationItemSelectedListener(this)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            intent = Intent(this, ChooseTrainingActivity::class.java)
            if (PatientCache.patient?.assignedExercises != null) {
                intent.putExtra("Patient", PatientCache.patient)
                startActivity(intent)
            }

        }
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    override fun onStart() {
        super.onStart()
        updateNavigationBarState()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.homeMenuButton -> {
                startActivity(Intent(this, HomeActivity::class.java))
                return true
            }
            R.id.notificationsMenuButton -> {
                startActivity(Intent(this, NotificationActivity::class.java))
                return true
            }
            R.id.activityMenuButton -> {
                if (PatientCache.patient?.assignedExercises != null) {
                    startActivity(
                        Intent(
                            this,
                            ChooseTrainingActivity::class.java
                        ).putExtra("Patient", PatientCache.patient)
                    )
                }
                return true
            }
            R.id.historyMenuButton -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                return true
            }
            R.id.profileMenuButton -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                return true
            }
            else -> return true
        }

    }

    private fun updateNavigationBarState() {
        val actionId: Int = getNavigationMenuItemId()
        selectBottomNavigationBarItem(actionId)
    }

    private fun selectBottomNavigationBarItem(itemId: Int) {
        val item = menuBar.menu.findItem(itemId)
        item.isChecked = true
    }

    abstract fun getContentViewId(): Int

    abstract fun getNavigationMenuItemId(): Int
}