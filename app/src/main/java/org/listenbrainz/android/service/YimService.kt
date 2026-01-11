package org.listenbrainz.android.service

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import org.listenbrainz.android.model.yimdata.YimPayload
interface YimService {
    // https://api.listenbrainz.org/1/stats/user/(user_name)/year-in-music/2022
    @GET("stats/user/{user_name}/year-in-music/2022")
    suspend fun getYimData(@Path("user_name") username: String): YimPayload
}