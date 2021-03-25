package nl.klimenko.sportlongen

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class GreetingActivity : AppCompatActivity() {

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_greeting)
        val signin = findViewById<ImageView>(R.id.signin_button)
        signin.setOnClickListener {
            signin.animate().apply {
                duration = 100
                translationY(5f)
            }.withEndAction {
                signin.animate().apply {
                    duration = 100
                    translationY(-5f)
                }.withEndAction {
                    intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }
            val terms = findViewById<ImageView>(R.id.terms_signin)
            terms.setOnClickListener {
                intent = Intent(this, TermsActivity::class.java)
                startActivity(intent)
            }
        }
    }