package org.listenbrainz.sharedtest.utils

import com.google.gson.Gson
import org.junit.Assert.fail
import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.model.SimilarUser
import org.listenbrainz.android.model.SimilarUserData
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.User
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
        const val alreadyFollowingError = "Jasjeet is already following user JasjeetTest"
        const val cannotFollowSelfError = "Whoops, cannot follow yourself."
    }
    
    val testSearchResult: SearchResult
        = SearchResult(
            users = listOf(
                User("Jasjeet"),
                User("JasjeetTest"),
                User("Jaw")
            )
        )
    
    val testSimilarUserSuccessData: SimilarUserData
        = SimilarUserData(
            payload = listOf(
                SimilarUser(0.592778543651705, "jivteshs20"),
                SimilarUser(0.2367332837331803, "akshaaatt"),
                SimilarUser(0.18639954307331036, "lucifer")
            )
        )
    
    val testFollowersSuccessData: SocialData
        get() {
            return SocialData(
                followers = listOf("jivteshs20","arsh331","Vac31.","JasjeetTest"),
                following = null,
                user = testUsername
            )
        }
    
    val testFollowingSuccessData : SocialData
        get() {
            return SocialData(
                followers = null,
                following = listOf("jivteshs20","akshaaatt","riksucks","lucifer"),
                user = testUsername
            )
        }
    
    val testYimData : YimPayload
        get() {
            return Gson().fromJson(loadResourceAsString("yim_data.json"), YimPayload::class.java)
        }
    
    const val testAuthHeader = "Bearer 8OC8as-1VpATqk-M79Kf-cdTw123a"
    const val testAccessToken = "8OC8as-1VpATqk-M79Kf-cdTw123a"
    
    const val testUsername = "Jasjeet"
    const val testFamiliarUser = "Jasjeettest"
    const val testSomeOtherUser = "AnotherUser"
    const val testUserDNE = "Some_User_That_Does_Not_Exist"
}