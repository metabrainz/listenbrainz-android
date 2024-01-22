package org.listenbrainz.android.service

import android.content.Context
import android.media.MediaMetadata
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.model.AdditionalInfo
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.ListenTrackMetadata
import org.listenbrainz.android.model.ListenType
import org.listenbrainz.android.model.PlayingTrack
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.dao.PendingListensDao
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Log.w
import org.listenbrainz.android.util.Resource

@HiltWorker
class ListenSubmissionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val appPreferences: AppPreferences,
    private val repository: ListensRepository,
    private val pendingListensDao: PendingListensDao
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        val token = appPreferences.lbAccessToken.get()
        if (token.isEmpty()) {
            d("ListenBrainz User token has not been set!")
            return Result.failure()
        }
        val duration = inputData.getInt(MediaMetadata.METADATA_KEY_DURATION, 0)
        if(duration in 1..30_000) {
            d("Track is too short to submit")
            return Result.failure()
        }
        val metadata = ListenTrackMetadata(
            artist = inputData.getString(MediaMetadata.METADATA_KEY_ARTIST),
            track = inputData.getString(MediaMetadata.METADATA_KEY_TITLE),
            release = inputData.getString(MediaMetadata.METADATA_KEY_ALBUM),
            additionalInfo = AdditionalInfo(
                durationMs = if (duration == 0) null else duration,
                mediaPlayer = inputData.getString(MediaMetadata.METADATA_KEY_WRITER)
                    ?.let { repository.getPackageLabel(it) },
                submissionClient = "ListenBrainz Android",
                submissionClientVersion = BuildConfig.VERSION_NAME
            )
        )
        
        // Our listen to submit
        val listen = ListenSubmitBody.Payload(
            timestamp = when (ListenType.SINGLE.code) {
                inputData.getString("TYPE") -> inputData.getLong(Constants.Strings.TIMESTAMP, 0)
                else -> null
            },
            metadata = metadata
        )
    
        val body = ListenSubmitBody().addListens(listen)
        
        body.listenType = inputData.getString("TYPE")
        
        // TODO: Inject dispatcher here and below as well.
        val response = withContext(Dispatchers.IO){
            repository.submitListen(token, body)
        }
        
        return when (response.status) {
            Resource.Status.SUCCESS -> {
                if (body.listenType == ListenType.PLAYING_NOW.code) {
                    d("Playing Now submitted")
                } else {
                    d("Listen submitted")
                }

                // Means conditions are met. Work manager automatically manages internet state.
                val pendingListens = pendingListensDao.getPendingListens()

                if (pendingListens.isNotEmpty()) {

                    val submission = withContext(Dispatchers.IO){
                        repository.submitListen(
                            token,
                            ListenSubmitBody().apply {
                                listenType = "import"
                                addListens(listensList = pendingListens)
                            }

                        )
                    }

                    when (submission.status) {
                        Resource.Status.SUCCESS -> {
                            // Empty all pending listens.
                            d("Pending listens submitted.")
                            pendingListensDao.deleteAllPendingListens()
                        }
                        else -> {
                            w("Could not submit pending listens.")
                        }
                    }
                }

                Result.success()

            }
            else -> {
                // In case of failure, we add this listen to pending list.
                if (inputData.getString("TYPE") == "single"){
                    if (response.error?.ordinal == ResponseError.BAD_REQUEST.ordinal) {
                        d("Submission failed, not saving listen because metadata is faulty.")
                    } else {
                        // We don't want to submit playing nows later.
                        d("Submission failed, listen saved.")
                        pendingListensDao.addListen(listen)
                    }
                }

                Result.failure()
            }
        }
    }
    
    companion object {
    
        /** Build a one time work request to submit a listen.
         * @param listenType Type of listen to submit.
         */
        fun buildWorkRequest(playingTrack: PlayingTrack, listenType: ListenType): OneTimeWorkRequest {
        
            val data = Data.Builder()
                .putString(MediaMetadata.METADATA_KEY_ARTIST, playingTrack.artist)
                .putString(MediaMetadata.METADATA_KEY_TITLE, playingTrack.title)
                .putInt(MediaMetadata.METADATA_KEY_DURATION, playingTrack.duration.toInt())
                .putString(MediaMetadata.METADATA_KEY_WRITER, playingTrack.pkgName)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, playingTrack.releaseName)
                .putString("TYPE", listenType.code)
                .putLong(Constants.Strings.TIMESTAMP, playingTrack.timestampSeconds)
                .build()
        
            /** We are not going to set network constraints as we want to minimize API calls
             * by bulk submitting listens.*/
            /*val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()*/
        
            return OneTimeWorkRequestBuilder<ListenSubmissionWorker>()
                .setInputData(data)
                //.setConstraints(constraints)
                .build()
        
        }
    }
}