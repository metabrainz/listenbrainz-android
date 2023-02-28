package org.listenbrainz.android.service

import org.listenbrainz.android.model.Blog
import retrofit2.http.GET

interface BlogService {
    @GET("blog.metabrainz.org/posts/")
    suspend fun getBlogs(): Blog
}