package org.listenbrainz.android.repository

import org.listenbrainz.android.model.Blog
import org.listenbrainz.android.util.Resource

interface BlogRepository {
    suspend fun fetchBlogs(): Resource<Blog>
}