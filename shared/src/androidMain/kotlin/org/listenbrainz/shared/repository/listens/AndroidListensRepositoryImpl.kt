package org.listenbrainz.shared.repository.listens

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.CoroutineDispatcher
import org.listenbrainz.shared.model.dao.PendingListensDao
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.service.ListensService
import org.listenbrainz.shared.service.UserService

class AndroidListensRepositoryImpl(
    service: ListensService,
    appPreferences: AppPreferences,
    userService: UserService,
    pendingListensDao: PendingListensDao,
    ioDispatcher: CoroutineDispatcher,
    private val androidContext: PlatformContext
): ListensRepositoryImpl(service,appPreferences,userService,pendingListensDao,ioDispatcher,androidContext) {

    override fun getPackageIcon(packageName: String): Drawable? {
        return try {
            androidContext.packageManager.getApplicationIcon(packageName)
        } catch(e:Exception){
            null
        }
    }

    override fun getPackageLabel(packageName: String): String {
        return try {
            val info = androidContext.packageManager.getApplicationInfo(packageName, 0)
            androidContext.packageManager.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}