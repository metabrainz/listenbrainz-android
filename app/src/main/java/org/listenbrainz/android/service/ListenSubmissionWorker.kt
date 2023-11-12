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
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.model.AdditionalInfo
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.ListenTrackMetadata
import org.listenbrainz.android.model.ListenType
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
        val token = appPreferences.getLbAccessToken()
        if (token.isEmpty()) {
            d("ListenBrainz User token has not been set!")
            return Result.failure()
        }
        if(inputData.getInt(MediaMetadata.METADATA_KEY_DURATION, 0) <= 30000) {
            d("Track is too short to submit")
            return Result.failure()
        }
        val metadata = ListenTrackMetadata(
            artist = inputData.getString(MediaMetadata.METADATA_KEY_ARTIST),
            track = inputData.getString(MediaMetadata.METADATA_KEY_TITLE),
            release = inputData.getString(MediaMetadata.METADATA_KEY_ALBUM),
            additionalInfo = AdditionalInfo(
                durationMs = inputData.getInt(MediaMetadata.METADATA_KEY_DURATION, 0),
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
                d("Listen submitted.")

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
                    // We don't want to submit playing nows later.
                    d("Submission failed, listen saved.")
                    pendingListensDao.addListen(listen)
                }

                Result.failure()
            }
        }
    }
}