package nl.klimenko.sportlongen.model

object ExerciseTypeList {
    var typesOfExercisesList: ArrayList<String> = ArrayList()

    init {
        typesOfExercisesList.add("Walking")
        typesOfExercisesList.add("Cycling")
        typesOfExercisesList.add("Running")
        typesOfExercisesList.add("Hiking")
        typesOfExercisesList.add("Swimming")
    }

    fun getANumberInDBForType(type: String): Int {
        when (type) {
            "Running" -> {
                return 1
            }
            "Cycling" -> {
                return 2
            }
            "Walking" -> {
                return 3
            }
            "Hiking" -> {
                return 6
            }
            else -> {
                return 7
            }
        }
    }

    fun getATypeByANumber(id: Int): String {
        when (id) {
            1 -> {
                return "Running"
            }
            2 -> {
                return "Cycling"
            }
            3 -> {
                return "Walking"
            }
            6 -> {
                return "Hiking"
            }
            else -> {
                return "Swimming"
            }
        }
    }
}

object SymptomsList {
    var symptomsList: ArrayList<String> = ArrayList()

    init {
        symptomsList.add("Shortness of breath")
        symptomsList.add("Chest tightness")
        symptomsList.add("Wheezing when exhaling")
        symptomsList.add("Wheezing when exhaling")

    }

}