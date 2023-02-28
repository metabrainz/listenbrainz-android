package org.listenbrainz.android.service

import android.widget.Toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class BrainzPlayerEventListener(
    private val brainzPlayerService : BrainzPlayerService
) : Player.Listener {
    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        if (reason == Player.STATE_READY && !playWhenReady) {
            brainzPlayerService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(brainzPlayerService, "BrainzPlayer Error", Toast.LENGTH_SHORT).show()
    }
}
