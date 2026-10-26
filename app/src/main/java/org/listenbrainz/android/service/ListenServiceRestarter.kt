package org.listenbrainz.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.listenbrainz.android.application.App
import org.listenbrainz.shared.repository.AppPreferences

/**
 * After an app update or a reboot, gets the notification listener bound again without the app
 * being opened (see App.startListenService).
 */
class ListenServiceRestarter : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val pending = goAsync()
        GlobalScope.launch {
            try {
                App.ensureKoinStarted(context)
                val koin = GlobalContext.get()
                App.startListenService(koin.get<AppPreferences>(), koin.get<ListenServiceState>())
            } finally {
                pending.finish()
            }
        }
    }
}
