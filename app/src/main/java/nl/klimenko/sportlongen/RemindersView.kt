package nl.klimenko.sportlongen

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.widget.CheckBox
import android.widget.ImageView
import nl.klimenko.sportlongen.configuration.SessionManager

class RemindersView {
    private lateinit var myDialog: Dialog
    private lateinit var sessionManager: SessionManager

    fun openRemindersView(dialog: Dialog, manager: SessionManager, c: Context, intent: Intent) {
        sessionManager = manager
        myDialog = dialog
        myDialog.setContentView(R.layout.ask_reminders)
        myDialog.window?.findViewById<ImageView>(R.id.ask_for_reminders_close)?.setOnClickListener {
            myDialog.dismiss()
        }
        myDialog.window?.findViewById<ImageView>(R.id.ask_for_reminders_continue)
            ?.setOnClickListener {
                val choiceNotification =
                    myDialog.window?.findViewById<CheckBox>(R.id.ask_for_reminders_checkbox_1)
                        ?.isChecked as Boolean
                val choiceVoice =
                    myDialog.window?.findViewById<CheckBox>(R.id.ask_for_reminders_checkbox_2)
                        ?.isChecked as Boolean
                val choiceNotificationNeverAsk =
                    myDialog.window?.findViewById<CheckBox>(R.id.ask_for_reminders_checkbox_3)
                        ?.isChecked as Boolean

                sessionManager.saveUserChoiceNotification(choiceNotification)
                sessionManager.saveUserChoiceVoiceNotification(choiceVoice)
                sessionManager.saveUserChoiceNotificationNeverAsk(choiceNotificationNeverAsk)

                if (!sessionManager.checkIfNotShowInhalerNotifications()) {
                    myDialog.let {
                        InhalerReminder().openRemindersView(it, sessionManager, c, intent)
                    }
                } else {
                    c.startActivity(intent)
                }
            }
        myDialog.show()
    }

}