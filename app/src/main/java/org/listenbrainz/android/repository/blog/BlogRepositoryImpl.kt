package org.listenbrainz.android.repository.blog

import androidx.annotation.WorkerThread
import org.listenbrainz.android.service.BlogService
import org.listenbrainz.android.model.Blog
import org.listenbrainz.shared.util.Resource
import org.listenbrainz.shared.util.Resource.Status.SUCCESS

class BlogRepositoryImpl(private val service: BlogService) : BlogRepository {

    @WorkerThread
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