package org.listenbrainz.sharedtest.testdata

import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.model.SimilarUser
import org.listenbrainz.android.model.SimilarUserData
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.User
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testFamiliarUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testSomeOtherUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername

object SocialRepositoryTestData {
    
    object ErrorUtil {
        const val userNotFoundError = "User Some_User_That_Does_Not_Exist not found"
        const val authHeaderNotFoundError = "You need to provide an Authorization header."
        const val alreadyFollowingError = "Jasjeet is already following user JasjeetTest"
        const val cannotFollowSelfError = "Whoops, cannot follow yourself."
    }
    
    val testSearchResult: SearchResult
            = SearchResult(
        users = listOf(
            User(testUsername),
            User(testFamiliarUser),
            User(testSomeOtherUser)
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
    
    val testFollowUnfollowSuccessResponse: SocialResponse
        get() {
            return SocialResponse(status = "ok")
        }
    
    val testFollowingSuccessData : SocialData
        get() {
            return SocialData(
                followers = null,
                following = listOf("jivteshs20","akshaaatt","riksucks","lucifer"),
                user = testUsername
            )
        }
}