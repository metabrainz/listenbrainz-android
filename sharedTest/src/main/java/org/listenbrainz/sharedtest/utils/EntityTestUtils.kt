package org.listenbrainz.sharedtest.utils

import org.junit.Assert.fail
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
    
    const val testAccessToken = "8OC8as-1VpATqk-M79Kf-cdTw123a"
    const val testAuthHeader = "Bearer $testAccessToken"
    
    const val testUsername = "Jasjeet"
    const val testFamiliarUser = "Jasjeettest"
    const val testSomeOtherUser = "AnotherUser"
    const val testUserDNE = "Some_User_That_Does_Not_Exist"
}