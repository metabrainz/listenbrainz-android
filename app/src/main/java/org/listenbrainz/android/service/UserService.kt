package org.listenbrainz.android.service

import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.user.TopArtists
import org.listenbrainz.android.model.user.UserSimilarityPayload
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
    @GET("user/{user_name}/listen-count")
    suspend fun getListenCount(@Path("user_name") username : String?) : Response<Listens?>

    @GET("user/{user_name}/similar-to/{other_user_name}")
    suspend fun getUserSimilarity(@Path("user_name") username: String? , @Path("other_user_name") otherUserName: String?) : Response<UserSimilarityPayload?>

    @GET("{user_name}/pins/current")
    suspend fun getUserCurrentPins(@Path("user_name") username: String?) : Response<PinnedRecording?>

    @GET("stats/user/{user_name}/artists?count=100")
    suspend fun getTopArtistsOfUser(@Path("user_name") username: String?) : Response<TopArtists>
}