package org.listenbrainz.android.service

import android.content.Context
import android.media.MediaMetadata
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.ListenTrackMetadata
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Resource

@HiltWorker
class ListenSubmissionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    val appPreferences: AppPreferences,
    val repository: ListensRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
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
        
        val response = withContext(Dispatchers.IO){
            repository.submitListen(token, body)
        }
        
        return if (response.status == Resource.Status.SUCCESS){
            d("Local listen submitted.")
            Result.success()
        }else{
            Result.failure()
        }
    }
}