package org.listenbrainz.android.data.sources

import kotlin.collections.HashMap

object QueryUtils {

    fun getQuery(arguments: HashMap<String, String>): String {
        val queryBuilder = StringBuilder()
        for ((key, value) in arguments){
            if (value.isNotEmpty()){
                queryBuilder.append("(").append(key).append(":").append(value).append(")")
            }
        }
        return queryBuilder.toString()
    }
}