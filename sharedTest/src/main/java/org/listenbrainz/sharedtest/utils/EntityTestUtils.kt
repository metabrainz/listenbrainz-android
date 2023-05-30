package org.listenbrainz.sharedtest.utils

import com.google.gson.Gson
import org.junit.Assert.fail
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.yimdata.YimPayload
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
    
    object ErrorUtil {
        const val userNotFoundError = "User Some_User_That_Does_Not_Exist not found"
        const val authHeaderNotFoundError = "You need to provide an Authorization header."
        const val alreadyFollowingError = "Jasjeet is already following user someotheruser"
    }
    
    val testYimData : YimPayload
        get() {
            return Gson().fromJson(loadResourceAsString("yim_data.json"), YimPayload::class.java)
        }
    
    val testFollowingSuccessData : SocialData
        get() {
            return SocialData(
                code = null,
                error = null,
                followers = listOf("jivteshs20","akshaaatt","riksucks","lucifer"),
                following = null,
                user = testUsername
            )
        }
    
    val testFollowingFailureData : SocialData
        get() {
            return SocialData(
                code = 404,
                error = ErrorUtil.userNotFoundError,
                followers = null,
                following = null,
                user = null
            )
        }
    
    const val testAuthHeader = "Bearer 8OC8as-1VpATqk-M79Kf-cdTw123a"
    
    const val testUsername : String = "Jasjeet"
    
    const val testSomeOtherUser = "Jasjeettest"
    
    const val testErrorUsername : String = "Some_User_That_Does_Not_Exist"
}