package org.listenbrainz.android.data.repository

import org.listenbrainz.android.data.sources.api.entities.mbentity.Collection
import org.listenbrainz.android.util.Resource

interface CollectionRepository {

    suspend fun fetchCollectionDetails(entity: String, id: String): Resource<String>

    suspend fun fetchCollections(editor: String, fetchPrivate: Boolean): Resource<MutableList<Collection>>
}