package org.listenbrainz.android.repository.user

import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.user.UserSimilarityPayload
import org.listenbrainz.android.util.Resource

interface UserRepository {
    suspend fun fetchUserListenCount (username: String?) : Resource<Listens?>
    suspend fun fetchListeningNow (username: String?) : Resource<Listens?>
    suspend fun fetchUserSimilarity(username: String? , otherUserName: String?) : Resource<UserSimilarityPayload?>
    suspend fun fetchUserCurrentPins(username: String?) : Resource<PinnedRecording?>
}