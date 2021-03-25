package nl.klimenko.sportlongen.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.notification_line.view.*
import nl.klimenko.sportlongen.R
import nl.klimenko.sportlongen.model.CompletedExercise
import nl.klimenko.sportlongen.model.Notification
/*
Adapter for the Notifications page
 */
class NotificationAdapter(
    var c: Context,
    private val notifications: List<Notification>
) : RecyclerView.Adapter<NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {

        val inflater = LayoutInflater.from(c)
        val itemView = inflater.inflate(R.layout.notification_line, parent, false)
        return NotificationViewHolder(itemView)
    }

    override fun getItemCount(): Int = notifications.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position], c)
    }
}

class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(
        notification: Notification,
        c: Context
    ) {
        itemView.notification_text.text = notification.message
    }
}