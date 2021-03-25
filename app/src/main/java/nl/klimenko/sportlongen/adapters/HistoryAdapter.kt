package nl.klimenko.sportlongen.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.history_activity_line.view.*
import nl.klimenko.sportlongen.R
import nl.klimenko.sportlongen.configuration.ContextObject
import nl.klimenko.sportlongen.configuration.HelperConverter
import nl.klimenko.sportlongen.configuration.ImageActivity
import nl.klimenko.sportlongen.model.CompletedExercise
import java.lang.String.*
import java.text.SimpleDateFormat
import java.util.*
/*
Adapter for the History page
 */
class HistoryAdapter(
    var c: Context,
    private val completedExercises: List<CompletedExercise>,
    private val historyListener: HistoryListener
) : RecyclerView.Adapter<HistoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(c)
        val itemView = inflater.inflate(R.layout.history_activity_line, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun getItemCount(): Int = completedExercises.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(completedExercises.get(position), c, historyListener)
    }

}

class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        completedExercise: CompletedExercise,
        c: Context,
        listener: HistoryListener
    ) {

        completedExercise.exerciseType?.title?.let {
            ImageActivity.getImageForActivityType(
                it
            )
        }?.let { itemView.historyImage.setImageResource(it) }

        itemView.historyDistance.text =
            format("%.2f", completedExercise.distance)

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val startDateTimeParsed = parser.parse(completedExercise.startDateTime as String)
        val endDateTimeParsed = parser.parse(completedExercise.endDateTime as String)
        val formatterDate = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
        val dateForSummary = formatterDate.format(endDateTimeParsed as Date)
        itemView.historyDate.text = dateForSummary
        val diff: Long =
            endDateTimeParsed.time - startDateTimeParsed.time
        val minutes = diff / 1000 / 60
        itemView.historyTime.text =
            HelperConverter.minutesIntToHoursMinutesSecondsString(minutes.toInt())
        if (completedExercise.avgHeartRate == null) {
            itemView.historyHeartRate.text = "-"
        } else {
            itemView.historyHeartRate.text = completedExercise.avgHeartRate.toString()
        }
        itemView.setOnClickListener {
            completedExercise.completedExerciseId?.let { it1 -> listener.onHistoryClicked(it1) }
        }
    }
}


