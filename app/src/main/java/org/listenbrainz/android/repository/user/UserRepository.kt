package org.listenbrainz.android.repository.user

import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.user.AllPinnedRecordings
import org.listenbrainz.android.model.user.TopArtists
import org.listenbrainz.android.model.user.UserFeedback
import org.listenbrainz.android.model.user.UserSimilarityPayload
import org.listenbrainz.android.util.Resource

interface UserRepository {
    suspend fun fetchUserListenCount (username: String?) : Resource<Listens?>
    suspend fun fetchUserSimilarity(username: String? , otherUserName: String?) : Resource<UserSimilarityPayload?>
    suspend fun fetchUserCurrentPins(username: String?) : Resource<PinnedRecording?>
    suspend fun fetchUserPins(username: String?) : Resource<AllPinnedRecordings?>
    //TODO: Move to artists VM once implemented
    suspend fun getTopArtists(username: String?): Resource<TopArtists>
    suspend fun getUserFeedback(username: String?, score: Int?): Resource<UserFeedback?>
}