package org.listenbrainz.android.application

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import org.listenbrainz.android.service.ListenService
import org.listenbrainz.android.util.UserPreferences.preferenceListeningEnabled


@HiltAndroidApp
class App : Application() {
    
    override fun onCreate() {
        super.onCreate()
        context = this
        loadCustomTypefaces()
        when {
            preferenceListeningEnabled && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP -> {
                startListenService()
            }
        }
    }

    private fun loadCustomTypefaces() {
        robotoLight = Typeface.createFromAsset(context!!.assets, "Roboto-Light.ttf")
    }

    fun startListenService() {
        val intent = Intent(this.applicationContext, ListenService::class.java)
        if (ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.CREATED) {
            startService(intent)
        }
    }

    fun stopListenService() {
        val intent = Intent(this.applicationContext, ListenService::class.java)
        stopService(intent)
    }
    val isNotificationServiceAllowed: Boolean
        get() {
            val listeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            return listeners != null && listeners.contains(packageName)
        }

    companion object {
        var context: App? = null
        var robotoLight: Typeface? = null
            private set
        val version: String
            get() = try {
                context?.packageManager?.getPackageInfo(context!!.packageName, 0)!!.versionName
            }
            catch (e: PackageManager.NameNotFoundException) {
                "unknown"
            }
    }
}