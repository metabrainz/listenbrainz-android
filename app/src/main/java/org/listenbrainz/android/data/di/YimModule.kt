package org.listenbrainz.android.data.di

import com.google.gson.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.data.repository.YimRepository
import org.listenbrainz.android.data.sources.api.ListenBrainzServiceGenerator
import org.listenbrainz.android.data.sources.api.YimApi
import org.listenbrainz.android.data.sources.api.entities.yimdata.Data
import org.listenbrainz.android.data.sources.api.entities.yimdata.TopRelease
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object YimModule {
    
    private val YimGson: Gson = GsonBuilder()
        /** Since a TopRelease may or may not contain "caaId", "caaReleaseMbid" or "releaseMbid", so we perform a check. */
        .registerTypeAdapter(
            TopRelease::class.java, JsonDeserializer<TopRelease>
            { jsonElement: JsonElement, _: Type, _: JsonDeserializationContext ->
                
                val element = Gson().fromJson(jsonElement, TopRelease::class.java)
                val jsonObject = jsonElement.asJsonObject
                
                return@JsonDeserializer TopRelease(
                    artistMbids = element.artistMbids,
                    artistName = element.artistName,
                    listenCount = element.listenCount,
                    releaseName = element.releaseName,
                    caaId = if (jsonObject.has("caa_id")) element.caaId else null,
                    caaReleaseMbid = if (jsonObject.has("caa_release_mbid")) element.caaReleaseMbid else null,
                    releaseMbid = if (jsonObject.has("release_mbid")) element.releaseMbid else null
                )
            }
        )
        /** Check if a user is new with 0 listens*/
        .registerTypeAdapter(
            Data::class.java, JsonDeserializer<Data>
            { jsonElement: JsonElement, _: Type, _: JsonDeserializationContext ->
                
                val element = Gson().fromJson(jsonElement, Data::class.java)
                return@JsonDeserializer if (element.totalListenCount == 0) null else element
                // "totalListenCount" field is our null checker.
            }
        )
        .create()
    
    @Singleton
    @Provides
    fun providesYimApi(): YimApi {
        return Retrofit.Builder()
            .baseUrl(ListenBrainzServiceGenerator.LISTENBRAINZ_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(YimGson))
            .build()
            .create(YimApi::class.java)
    }
    
    @Singleton
    @Provides
    fun providesYimRepository(api: YimApi)
    = YimRepository(api)
    
}
