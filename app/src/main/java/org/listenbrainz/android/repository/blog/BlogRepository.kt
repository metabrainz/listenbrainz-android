package org.listenbrainz.android.repository.blog

import org.listenbrainz.android.model.Blog
import org.listenbrainz.shared.util.Resource

interface BlogRepository {
    suspend fun fetchBlogs(): Resource<Blog>
}