package org.listenbrainz.shared.repository

import org.listenbrainz.shared.social.ListenBrainzExternalServices
import org.listenbrainz.shared.social.Resource

interface ListensRepository{
    suspend fun getLinkedServices(token: String?, username: String?) : Resource<ListenBrainzExternalServices>
}