package org.listenbrainz.android.service

import android.app.Service.STOP_FOREGROUND_DETACH
import android.os.Build
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import org.listenbrainz.android.util.Log

class BrainzPlayerEventListener(
    private val brainzPlayerService : BrainzPlayerService
) : Player.Listener {
    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (reason == Player.STATE_IDLE && !playWhenReady) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                brainzPlayerService.stopForeground(STOP_FOREGROUND_DETACH)
            }else {
                brainzPlayerService.stopForeground(false)
            }
        }
        super.onPlayWhenReadyChanged(playWhenReady, reason)
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Log.e("BrainzPlayer error")
    }
}
