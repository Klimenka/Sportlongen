package nl.klimenko.sportlongen.adapters

import nl.klimenko.sportlongen.model.CompletedExercise
/*
On completed exercise click Listener (for the History page)
 */
interface HistoryListener {
    fun onHistoryClicked(exerciseId: Int)
}