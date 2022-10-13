package org.listenbrainz.android.data.repository

import androidx.annotation.WorkerThread
import org.listenbrainz.android.data.sources.CollectionUtils
import org.listenbrainz.android.data.sources.api.CollectionService
import org.listenbrainz.android.data.sources.api.entities.mbentity.Collection
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Resource.Status.FAILED
import org.listenbrainz.android.util.Resource.Status.SUCCESS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(val service: CollectionService) : CollectionRepository{

    @WorkerThread
    override suspend fun fetchCollectionDetails(entity: String, id: String): Resource<String> {
        return try {
            val result = service.getCollectionContents(entity, id)
            Resource(SUCCESS, result.string())
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.failure()
        }
    }

    @WorkerThread
    override suspend fun fetchCollections(editor: String, fetchPrivate: Boolean): Resource<MutableList<Collection>> {
        return try {
            val response = when {
                fetchPrivate -> service.getAllUserCollections(editor, "user-collections")
                else -> service.getPublicUserCollections(editor)
            }
            val collections = CollectionUtils.setGenericCountParameter(response.string())
            Resource(SUCCESS, collections)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(FAILED, null)
        }
    }
}