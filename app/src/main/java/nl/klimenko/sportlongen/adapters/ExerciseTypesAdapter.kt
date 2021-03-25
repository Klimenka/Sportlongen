package nl.klimenko.sportlongen.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activities.view.*
import nl.klimenko.sportlongen.ExerciseListener
import nl.klimenko.sportlongen.R
import nl.klimenko.sportlongen.model.Exercise

/*
This adapter plays a role in plan activity screen as a scrollable types icons
 */
class ExerciseTypesAdapter(
    var c: Context,
    private val items: ArrayList<Exercise>,
    private val exerciseListener: ExerciseListener
) :
    RecyclerView.Adapter<TypesViewHolder>() {
    private var rowIndex = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypesViewHolder {
        val inflater = LayoutInflater.from(c)
        val itemView = inflater.inflate(R.layout.activities, parent, false)
        return TypesViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TypesViewHolder, position: Int) {
        holder.bind(items.get(position), c, exerciseListener)
        holder.itemView.setOnClickListener {
            rowIndex = position
            exerciseListener.onExerciseClicked(items.get(position))
            notifyDataSetChanged()
        }
        if (rowIndex == position) {
            holder.itemView.typeExerciseCard.setCardBackgroundColor(c.getColor(R.color.colorLightOrange))
        } else {
            holder.itemView.typeExerciseCard.setCardBackgroundColor(c.getColor(R.color.colorWhite))
        }
    }
}

class TypesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(
        item: Exercise,
        c: Context,
        exerciseListener: ExerciseListener
    ) {
        when (item.exerciseName) {
            "Cycling" -> itemView.plan_activity_image.setImageResource(R.drawable.planning_cycling)
            "Running" -> itemView.plan_activity_image.setImageResource(R.drawable.planning_running)
            "Hiking" -> itemView.plan_activity_image.setImageResource(R.drawable.hiking)
            "Walking" -> itemView.plan_activity_image.setImageResource(R.drawable.planning_walking)
            "Swimming" -> itemView.plan_activity_image.setImageResource(R.drawable.planning_swimming)

        }
        itemView.plan_activity_text.text = item.exerciseName

    }
}