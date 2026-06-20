package org.listenbrainz.shared.service

import de.jensklingenberg.ktorfit.http.GET
import org.listenbrainz.shared.model.Blog

interface BlogService {
    @GET("blog.metabrainz.org/posts/")
    suspend fun getBlogs(): Blog
}