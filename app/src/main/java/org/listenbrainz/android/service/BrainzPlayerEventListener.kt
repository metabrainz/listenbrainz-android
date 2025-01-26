package org.listenbrainz.android.service

import android.app.Service.STOP_FOREGROUND_DETACH
import android.os.Build
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.DISCONTINUITY_REASON_AUTO_TRANSITION
import com.google.android.exoplayer2.Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT
import org.listenbrainz.android.util.Log

class BrainzPlayerEventListener(
    private val brainzPlayerService: BrainzPlayerService
) : Player.Listener {
    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (reason == Player.STATE_IDLE && !playWhenReady) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                brainzPlayerService.stopForeground(STOP_FOREGROUND_DETACH)
            } else {
                brainzPlayerService.stopForeground(false)
            }
        }
        super.onPlayWhenReadyChanged(playWhenReady, reason)
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        super.onPositionDiscontinuity(oldPosition, newPosition, reason)

        //updating current playing index when song auto transitions or song change from notification
        when (reason) {
            DISCONTINUITY_REASON_SEEK_ADJUSTMENT, DISCONTINUITY_REASON_AUTO_TRANSITION -> {
                brainzPlayerService.appPreferences.currentPlayable =
                    brainzPlayerService.appPreferences.currentPlayable?.copy(currentSongIndex = newPosition.mediaItemIndex)
            }

            else -> {}
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Log.e("BrainzPlayer error")
    }
}
