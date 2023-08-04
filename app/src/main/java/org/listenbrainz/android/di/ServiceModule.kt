package org.listenbrainz.android.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.listenbrainz.android.model.yimdata.YimData
import org.listenbrainz.android.service.BlogService
import org.listenbrainz.android.service.FeedService
import org.listenbrainz.android.service.ListensService
import org.listenbrainz.android.service.SocialService
import org.listenbrainz.android.service.YimService
import org.listenbrainz.android.util.CachingInterceptor
import org.listenbrainz.android.util.Constants.LISTENBRAINZ_API_BASE_URL
import org.listenbrainz.android.util.HeaderInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import javax.inject.Singleton

@Module(includes = [
    AppModule::class,
    DispatcherModule::class
])
@InstallIn(SingletonComponent::class)
class ServiceModule {
    
    private fun constructRetrofit(headerInterceptor: HeaderInterceptor, cachingInterceptor: CachingInterceptor,cache: Cache): Retrofit =
        Retrofit.Builder()
            .client(
                OkHttpClient()
                    .newBuilder()
                    .cache(cache)
                    .addInterceptor(headerInterceptor)
                    .addInterceptor(cachingInterceptor)
                    // .addInterceptor (HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build()
            )
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    
    
    @get:Singleton
    @get:Provides
    val blogService: BlogService = Retrofit.Builder()
        .baseUrl("https://public-api.wordpress.com/rest/v1.1/sites/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(BlogService::class.java)

    
    @Singleton
    @Provides
    fun providesListensService(headerInterceptor: HeaderInterceptor, cachingInterceptor: CachingInterceptor, @OkHttpCache cache: Cache): ListensService =
        constructRetrofit(headerInterceptor, cachingInterceptor, cache)
            .create(ListensService::class.java)
    
    
    @Singleton
    @Provides
    fun providesSocialService(headerInterceptor: HeaderInterceptor, cachingInterceptor: CachingInterceptor, @OkHttpCache cache: Cache): SocialService =
        constructRetrofit(headerInterceptor, cachingInterceptor, cache)
            .create(SocialService::class.java)
    
    
    @Singleton
    @Provides
    fun providesFeedService(headerInterceptor: HeaderInterceptor, cachingInterceptor: CachingInterceptor, @OkHttpCache cache: Cache): FeedService =
        constructRetrofit(headerInterceptor, cachingInterceptor, cache)
            .create(FeedService::class.java)
    
    /* YIM */
    
    private val yimGson: Gson by lazy {
        
        GsonBuilder()
            /** Since a TopRelease may or may not contain "caaId", "caaReleaseMbid" or "releaseMbid", so we perform a check. */
            /*.registerTypeAdapter(
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
        )*/
            /** Check if a user is new with 0 listens*/
            .registerTypeAdapter(
                YimData::class.java, JsonDeserializer<YimData>
                { jsonElement: JsonElement, _: Type, _: JsonDeserializationContext ->
                
                    val element = Gson().fromJson(jsonElement, YimData::class.java)
                    return@JsonDeserializer if (element.totalListenCount == 0) null else element
                    // "totalListenCount" field is our null checker.
                }
            )
            .create()
    }

    
    @get:Singleton
    @get:Provides
    val yimService: YimService = Retrofit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(yimGson))
            .build()
            .create(YimService::class.java)
}