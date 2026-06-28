package org.listenbrainz.shared.repository.listens

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.CoroutineDispatcher
import org.listenbrainz.shared.applicationContext
import org.listenbrainz.shared.model.dao.PendingListensDao
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.service.ListensService
import org.listenbrainz.shared.service.UserService

class AndroidListensRepositoryImpl(
    service: ListensService,
    appPreferences: AppPreferences,
    userService: UserService,
    pendingListensDao: PendingListensDao,
    ioDispatcher: CoroutineDispatcher
): ListensRepositoryImpl(service,appPreferences,userService,pendingListensDao,ioDispatcher) {

    override fun getPackageIcon(packageName: String): Drawable? {
        return try {
            applicationContext.packageManager.getApplicationIcon(packageName)
        } catch(e:Exception){
            null
        }
    }

    override fun getPackageLabel(packageName: String): String {
        return try {
            val info = applicationContext.packageManager.getApplicationInfo(packageName, 0)
            applicationContext.packageManager.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}