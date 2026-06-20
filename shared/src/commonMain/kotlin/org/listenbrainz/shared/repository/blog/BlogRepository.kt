package org.listenbrainz.shared.repository.blog

import org.listenbrainz.shared.model.Blog
import org.listenbrainz.shared.util.Resource

interface BlogRepository {
    suspend fun fetchBlogs(): Resource<Blog>
}