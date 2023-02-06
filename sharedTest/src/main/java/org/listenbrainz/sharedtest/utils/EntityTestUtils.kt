package org.listenbrainz.sharedtest.utils

import com.google.gson.Gson
import org.junit.Assert.fail
import org.listenbrainz.android.data.sources.api.entities.yimdata.YimData
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object EntityTestUtils {

    fun loadResourceAsString(resource: String?): String {
        val builder = StringBuilder()
        try {
            this.javaClass.classLoader?.getResourceAsStream(resource).use { inputStream ->
            //ClassLoader.getSystemClassLoader().getResourceAsStream(resource).use { inputStream ->
                BufferedReader(
                    InputStreamReader(inputStream)
                ).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) builder.append(line)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            fail()
        }
        return builder.toString()
    }
    
    val testYimData : YimData
        get() {
            return Gson().fromJson(loadResourceAsString("yim_data.json"), YimData::class.java)
        }
    
    val testYimUsername : String
        get() = "jasjeet"
    
}