package org.listenbrainz.android.data.repository

import org.listenbrainz.android.data.sources.api.entities.blog.Blog
import org.listenbrainz.android.util.Resource

interface BlogRepository {
    suspend fun fetchBlogs(): Resource<Blog>
}