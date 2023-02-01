package org.listenbrainz.android

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.HiltAndroidApp
import org.listenbrainz.android.presentation.Configuration
import org.listenbrainz.android.presentation.UserPreferences.preferenceListeningEnabled
import org.listenbrainz.android.presentation.features.listens.submitter.ListenService

@HiltAndroidApp
class App : Application() {
    
    override fun onCreate() {
        super.onCreate()
        context = this
        loadCustomTypefaces()
        if (preferenceListeningEnabled && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) startListenService()
    }

    private fun loadCustomTypefaces() {
        robotoLight = Typeface.createFromAsset(context!!.assets, "Roboto-Light.ttf")
    }

    fun startListenService() {
        val intent = Intent(this.applicationContext, ListenService::class.java)
        startService(intent)
    }

    fun stopListenService() {
        val intent = Intent(this.applicationContext, ListenService::class.java)
        stopService(intent)
    }
    fun getApplication(): Application {
        return context!!
    }
    fun getContext(): Context {
        return getApplication().applicationContext
    }
    val isNotificationServiceAllowed: Boolean
        get() {
            val listeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            return listeners != null && listeners.contains(packageName)
        }
    val isOnline: Boolean
        get() {
            val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val nw      = connectivityManager.activeNetwork ?: return false
                val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
                return when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    //for other device how are able to connect with Ethernet
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    //for check internet over Bluetooth
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                    else -> false
                }
            } else {
                return connectivityManager.activeNetworkInfo?.isConnected ?: false
            }
        }

    companion object {
        const val WEBSITE_BASE_URL = "https://musicbrainz.org/"
        const val PICARD_OPENALBUM_URL = "http://%s:%s/openalbum?id=%s"
        var context: App? = null
        var robotoLight: Typeface? = null
            private set
        val userAgent: String
            get() = Configuration.USER_AGENT + "/" + version
        val clientId: String
            get() = Configuration.CLIENT_NAME + "-" + version
        val version: String
            get() = try {
                context!!.packageManager.getPackageInfo(context!!.packageName, 0).versionName
            }
            catch (e: PackageManager.NameNotFoundException) {
                "unknown"
            }
    }
}