package org.listenbrainz.android.data.sources.api

import org.listenbrainz.android.data.sources.api.entities.blog.Blog
import retrofit2.http.GET

interface BlogService {
    @GET("blog.metabrainz.org/posts/")
    suspend fun getBlogs(): Blog
}