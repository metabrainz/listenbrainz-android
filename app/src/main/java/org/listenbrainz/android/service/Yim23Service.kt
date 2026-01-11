package org.listenbrainz.android.service

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import org.listenbrainz.android.model.yimdata.Yim23Payload
// TODO: TO BE REMOVED WHEN YIM GOES LIVE
interface Yim23Service {
    @GET("stats/user/{user_name}/year-in-music/2023")
    suspend fun getYimData(@Path("user_name") username: String): Yim23Payload
}