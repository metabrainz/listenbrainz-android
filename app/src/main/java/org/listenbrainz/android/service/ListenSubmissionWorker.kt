package org.listenbrainz.android.service

import android.R.attr.duration
import android.content.Context
import android.media.MediaMetadata
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.shared.model.AdditionalInfo
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.ListenTrackMetadata
import org.listenbrainz.android.model.ListenType
import org.listenbrainz.android.model.PlayingTrack
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.dao.PendingListensDao
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Resource

class ListenSubmissionWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val appPreferences: AppPreferences by inject()
    private val repository: ListensRepository by inject()
    private val pendingListensDao: PendingListensDao by inject()
    
    override suspend fun doWork(): Result {
        val token = appPreferences.lbAccessToken.get()
        if (token.isEmpty()) {
            Log.d("ListenBrainz User token has not been set!")
            return Result.failure()
        }
        val duration = inputData.getLong(MediaMetadata.METADATA_KEY_DURATION, 0).run {
            when (this) {
                0L -> null
                in 1..30_000 -> {
                    Log.d("Track is too short to submit, duration: $duration")
                    return Result.failure()
                }
                else -> this
            }
        }

        val metadata = ListenTrackMetadata(
            artist = inputData.getString(MediaMetadata.METADATA_KEY_ARTIST),
            track = inputData.getString(MediaMetadata.METADATA_KEY_TITLE),
            release = inputData.getString(MediaMetadata.METADATA_KEY_ALBUM),
            additionalInfo = AdditionalInfo(
                durationMs = duration?.toInt(),
                mediaPlayer = inputData.getString(MediaMetadata.METADATA_KEY_WRITER)
                    ?.let { repository.getPackageLabel(it) },
                submissionClient = "ListenBrainz Android",
                submissionClientVersion = BuildConfig.VERSION_NAME
            )
        )

        if (!metadata.isValid()) {
            Log.d("Track metadata is not valid: $metadata")
            return Result.failure()
        }
        
        // Our listen to submit
        val listen = ListenSubmitBody.Payload(
            timestamp = when (ListenType.SINGLE.code) {
                inputData.getString(LISTEN_TYPE) -> inputData.getLong(Constants.Strings.TIMESTAMP, 0)
                else -> null
            },
            metadata = metadata
        )
    
        val body = ListenSubmitBody().addListens(listen)
        
        body.listenType = inputData.getString(LISTEN_TYPE)
        
        // TODO: Inject dispatcher here and below as well.
        val response = withContext(Dispatchers.IO) {
            repository.submitListen(token, body)
        }
        
        return when (response.status) {
            Resource.Status.SUCCESS -> {
                if (body.listenType == ListenType.PLAYING_NOW.code) {
                    Log.d("Playing Now submitted")
                } else {
                    Log.d("Listen submitted")
                }

                // Means conditions are met. Work manager automatically manages internet state.
                val pendingListens = pendingListensDao.getPendingListens()

                if (pendingListens.isNotEmpty()) {
                    val submission = withContext(Dispatchers.IO) {
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
                            Log.d("Pending listens submitted.")
                            pendingListensDao.deleteAllPendingListens()
                        }
                        else -> {
                            Log.w("Could not submit pending listens.")
                        }
                    }
                }

                Result.success()

            }
            else -> {
                // In case of failure, we add this listen to pending list.
                if (inputData.getString("TYPE") == "single") {
                    // We don't want to submit playing nows later.
                    if (response.error is ResponseError.BadRequest) {
                        Log.e(
                            "Submission failed, not saving listen because metadata is faulty."
                            + "\n Server response: ${response.error.toast}" + "\n POST Request Body: $body"
                        )
                    } else {
                        Log.e("Submission failed, listen saved.")
                        pendingListensDao.addListen(listen)
                    }
                } else {
                    // Playing now was not submitted.
                    Log.e("Could not submit playing now. Reason: " + (response.error?.toast ?: "Unknown"))
                }

                Result.failure()
            }
        }
    }
    
    companion object {
        const val LISTEN_TYPE = "TYPE"

    
        /** Build a one time work request to submit a listen.
         * @param listenType Type of listen to submit.
         */
        fun buildWorkRequest(playingTrack: PlayingTrack, listenType: ListenType): OneTimeWorkRequest {
            val data = Data.Builder()
                .putString(MediaMetadata.METADATA_KEY_ARTIST, playingTrack.artist)
                .putString(MediaMetadata.METADATA_KEY_TITLE, playingTrack.title)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, playingTrack.duration)
                .putString(MediaMetadata.METADATA_KEY_WRITER, playingTrack.pkgName)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, playingTrack.releaseName)
                .putString(LISTEN_TYPE, listenType.code)
                .putLong(Constants.Strings.TIMESTAMP, playingTrack.timestampSeconds)
                .build()
        
            return OneTimeWorkRequestBuilder<ListenSubmissionWorker>()
                .setInputData(data)
                .build()
        }
    }
}