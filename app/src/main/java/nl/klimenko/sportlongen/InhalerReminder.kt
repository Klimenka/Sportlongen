package nl.klimenko.sportlongen

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.widget.CheckBox
import android.widget.ImageView
import nl.klimenko.sportlongen.configuration.SessionManager

class InhalerReminder {
    private lateinit var myDialog: Dialog
    lateinit var sessionManager: SessionManager

    fun openRemindersView(dialog: Dialog, manager: SessionManager, c: Context, intent: Intent) {
        sessionManager = manager
        myDialog = dialog
        myDialog.setContentView(R.layout.inhaler_reminder)
        myDialog.window?.findViewById<ImageView>(R.id.inahler_reminder_close)?.setOnClickListener {
            myDialog.dismiss()
        }
        myDialog.window?.findViewById<ImageView>(R.id.inahler_reminder_continue)
            ?.setOnClickListener {
                val choiceNotificationInhaler =
                    myDialog.window?.findViewById<CheckBox>(R.id.inahler_reminder_checkbox)
                        ?.isChecked as Boolean
                sessionManager.saveUserChoiceInhalerNotificationNeverAsk(choiceNotificationInhaler)
                c.startActivity(intent)
            }
        myDialog.show()
    }

}