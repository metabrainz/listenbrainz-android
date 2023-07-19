package org.listenbrainz.android.service

import android.content.Context
import android.media.MediaMetadata
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.ListenTrackMetadata
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.repository.ListensRepository
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Log.d
import javax.inject.Inject

class ListenSubmissionWorker @Inject constructor(
    val appPreferences: AppPreferences,
    val repository: ListensRepository,
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    
    override fun doWork(): Result {
        val token = appPreferences.lbAccessToken
        if (token.isNullOrEmpty()) {
            d("ListenBrainz User token has not been set!")
            return Result.failure()
        }
        if(inputData.getInt(MediaMetadata.METADATA_KEY_DURATION, 0) <= 30000) {
            d("Track is too short to submit")
            return Result.failure()
        }
        val metadata = ListenTrackMetadata()
    
        // Main metadata
        metadata.artist = inputData.getString(MediaMetadata.METADATA_KEY_ARTIST)
        metadata.track = inputData.getString(MediaMetadata.METADATA_KEY_TITLE)
        metadata.release = inputData.getString(MediaMetadata.METADATA_KEY_ALBUM)
    
        // Duration
        metadata.additionalInfo.duration_ms = inputData.getInt(MediaMetadata.METADATA_KEY_DURATION, 0)
    
        // Setting player
        val player = inputData.getString(MediaMetadata.METADATA_KEY_WRITER)
        if (player != null)
            metadata.additionalInfo.media_player = repository.getPackageLabel(player)
    
        val body = ListenSubmitBody()
        body.addListen(
            timestamp = if(inputData.getString("TYPE") == "single") inputData.getLong(Constants.Strings.TIMESTAMP, 0) else null,
            metadata = metadata,
            insertedAt = System.currentTimeMillis().toInt()
        )
        body.listenType = inputData.getString("TYPE")
        
        val response = repository.submitListen("Token $token", body)
    
        /*return if (response?.isSuccessful == true){
            d("Local listen submitted.")
            Result.success()
        }else{
            Result.failure()
        }*/
        return Result.success()
    }
}