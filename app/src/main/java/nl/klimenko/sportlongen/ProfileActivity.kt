package nl.klimenko.sportlongen

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import nl.klimenko.sportlongen.configuration.PatientCache

class ProfileActivity : BaseActivity() {
    private lateinit var menuBar: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nameField = findViewById<TextView>(R.id.profile_name)
        nameField.text = (PatientCache.patient?.user?.firstName) + " " + (PatientCache.patient?.user?.surName)
        val cityField = findViewById<TextView>(R.id.city)
        cityField.text = PatientCache.patient?.city
        val logoutField = findViewById<TextView>(R.id.logout)


        logoutField.setOnClickListener {
            intent = Intent(this, LogoutActivity::class.java)
            startActivity(intent)
        }
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_profile
    }

    override fun getNavigationMenuItemId(): Int {
        return R.id.profileMenuButton
    }
}