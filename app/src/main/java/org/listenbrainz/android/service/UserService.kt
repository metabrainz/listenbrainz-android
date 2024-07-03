package org.listenbrainz.android.service

import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.user.TopArtists
import org.listenbrainz.android.model.user.UserFeedback
import org.listenbrainz.android.model.user.UserSimilarityPayload
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
    @GET("user/{user_name}/listen-count")
    suspend fun getListenCount(@Path("user_name") username : String?) : Response<Listens?>

    @GET("user/{user_name}/similar-to/{other_user_name}")
    suspend fun getUserSimilarity(@Path("user_name") username: String? , @Path("other_user_name") otherUserName: String?) : Response<UserSimilarityPayload?>

    @GET("{user_name}/pins/current")
    suspend fun getUserCurrentPins(@Path("user_name") username: String?) : Response<PinnedRecording?>

    @GET("stats/user/{user_name}/artists?count=100")
    suspend fun getTopArtistsOfUser(@Path("user_name") username: String?) : Response<TopArtists>

    @GET("feedback/user/{user_name}/get-feedback?metadata=true")
    suspend fun getUserFeedback(@Path("user_name") username: String?, @Query("score") score: Int?) : Response<UserFeedback?>
}