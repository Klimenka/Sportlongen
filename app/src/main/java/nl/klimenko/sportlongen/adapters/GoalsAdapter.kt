package nl.klimenko.sportlongen.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.goals.view.*
import nl.klimenko.sportlongen.R
import nl.klimenko.sportlongen.configuration.ContextObject
import nl.klimenko.sportlongen.configuration.ImageActivity
import nl.klimenko.sportlongen.model.AssignedExercise
import nl.klimenko.sportlongen.model.CompletedExercise
/*
This is adapter for goals set by the care provider. It is displayed on the Homepage
 */
class GoalsAdapter(
    var c: Context,
    private val items: List<AssignedExercise>,
    private val completedExercisesThisWeek: List<CompletedExercise>?
) : RecyclerView.Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(c)
        val itemView = inflater.inflate(R.layout.goals, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(items.get(position), completedExercisesThisWeek, c)

    }
}

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        item: AssignedExercise,
        completedExercisesThisWeek: List<CompletedExercise>?,
        c: Context
    ) {

        itemView.goalsImage.setImageResource(ImageActivity.getImageForActivityType(item.exerciseType.title))
        itemView.goalsTimesAWeek.text =
            item.timesAWeek.toString() + " " + ContextObject.context?.getString(R.string.times_a_week)

        val numberOfExercisesDone: Int?

        numberOfExercisesDone = if (completedExercisesThisWeek != null) {
            val isLeftForActivityType = { exercise: CompletedExercise ->
                exercise.exerciseType?.title.equals(
                    item.exerciseType.title,
                    ignoreCase = true
                )
            }
            completedExercisesThisWeek.count(isLeftForActivityType)
        } else {
            0
        }
        if (item.timesAWeek - numberOfExercisesDone <= 0) {
            itemView.goalsYesImage.visibility = View.VISIBLE
            itemView.goalsRedLetters.visibility = View.GONE
        } else {
            itemView.goalsYesImage.visibility = View.GONE
            itemView.goalsRedLetters.visibility = View.VISIBLE
            itemView.goalsRedLetters.text =
                (item.timesAWeek - numberOfExercisesDone).toString() + " " + c.getString(R.string.left)
        }
    }
}


