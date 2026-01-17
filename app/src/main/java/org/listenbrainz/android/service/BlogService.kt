package org.listenbrainz.android.service

import de.jensklingenberg.ktorfit.http.GET
import org.listenbrainz.android.model.Blog

interface BlogService {
    @GET("blog.metabrainz.org/posts/")
    suspend fun getBlogs(): Blog
}