package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.android.data.repository.CollectionRepository
import org.listenbrainz.android.data.sources.CollectionUtils
import org.listenbrainz.android.data.sources.api.entities.mbentity.Collection
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.utils.EntityTestUtils

class MockCollectionRepository : CollectionRepository {

    override suspend fun fetchCollectionDetails(entity: String, id: String): Resource<String> {
        return Resource(Resource.Status.SUCCESS, EntityTestUtils.loadResourceAsString( "collection_details.json"))
    }

    override suspend fun fetchCollections(editor: String, fetchPrivate: Boolean): Resource<MutableList<Collection>> {
        val response = if (fetchPrivate)
            EntityTestUtils.loadResourceAsString("collections_private.json")
        else
            EntityTestUtils.loadResourceAsString("collections_public.json")

        val collections = CollectionUtils.setGenericCountParameter(response)
        return Resource(Resource.Status.SUCCESS, collections)
    }
}