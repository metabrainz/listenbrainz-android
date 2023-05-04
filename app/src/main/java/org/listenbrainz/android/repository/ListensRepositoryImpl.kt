package org.listenbrainz.android.repository

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.annotation.WorkerThread
import org.listenbrainz.android.application.App
import org.listenbrainz.android.model.CoverArt
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.service.ListensService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Resource.Status.FAILED
import org.listenbrainz.android.util.Resource.Status.SUCCESS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListensRepositoryImpl @Inject constructor(val service: ListensService) : ListensRepository {

    @WorkerThread
    override suspend fun fetchUserListens(userName: String): Resource<List<Listen>> {
        return try {
            val response = service.getUserListens(user_name = userName, count = 100)
            Resource(SUCCESS, response.payload.listens)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(FAILED, null)
        }
    }

    override suspend fun fetchCoverArt(MBID: String): Resource<CoverArt> {
        return try {
            val coverArt = service.getCoverArt(MBID)
            Resource(SUCCESS, coverArt)
        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.failure()
        }
    }
    
    /** Retrieve any installed application's icon. If the requested application is not installed, null is returned.
     *
     * Usage:
     * ```
     * Image(
     *    painter = rememberDrawablePainter( drawable = getPackageIcon(packageName) ),
     *    contentDescription = "Example Description"
     * )
     * ```
     * */
    override fun getPackageIcon(packageName: String): Drawable? {
        return try {
            App.context!!.packageManager.getApplicationIcon(packageName)
        }
        catch (e: Exception) {
            null
        }
    }

    override fun getPackageLabel(packageName: String): String {
        return try {
            val info = App.context!!.packageManager.getApplicationInfo(packageName, 0)
            App.context!!.packageManager.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}