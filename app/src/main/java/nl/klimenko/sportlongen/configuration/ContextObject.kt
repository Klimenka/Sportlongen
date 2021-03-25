package nl.klimenko.sportlongen.configuration

import android.content.Context

object ContextObject {
    var context: Context? = null

    fun setup(contextSet: Context){
        context = contextSet
    }
    }