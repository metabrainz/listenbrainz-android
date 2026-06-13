package org.listenbrainz.shared.repository.listens

import kotlinx.coroutines.CoroutineDispatcher
import org.listenbrainz.shared.model.dao.PendingListensDao
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.service.ListensService
import org.listenbrainz.shared.service.UserService

class IosListensRepositoryImpl(
    service: ListensService,
    appPreference: AppPreferences,
    userService: UserService,
    pendingListensDao: PendingListensDao,
    ioDispatcher: CoroutineDispatcher,
    context: PlatformContext
): ListensRepositoryImpl(service,appPreference,userService,pendingListensDao,ioDispatcher,context)  {

    override fun getPackageIcon(packageName: String): Any? {
        return null
    }

    override fun getPackageLabel(packageName: String): String {
        return packageName
    }
}