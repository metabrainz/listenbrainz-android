package org.listenbrainz.shared.repository.blog

import org.listenbrainz.shared.service.BlogService
import org.listenbrainz.shared.model.Blog
import org.listenbrainz.shared.util.Resource
import org.listenbrainz.shared.util.Resource.Status.SUCCESS

class BlogRepositoryImpl(private val service: BlogService) : BlogRepository {

    override suspend fun fetchBlogs(): Resource<Blog> {
        return try {
            val result = service.getBlogs()
            Resource(SUCCESS, result)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.failure()
        }
    }
}