package org.listenbrainz.shared.service

import dev.brewkits.kmpworkmanager.annotations.Worker
import dev.brewkits.kmpworkmanager.background.domain.AndroidWorker
import dev.brewkits.kmpworkmanager.background.domain.WorkerEnvironment
import dev.brewkits.kmpworkmanager.background.domain.WorkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.listenbrainz.shared.model.AdditionalInfo
import org.listenbrainz.shared.model.ListenSubmitBody
import org.listenbrainz.shared.model.ListenTrackMetadata
import org.listenbrainz.shared.model.ListenType
import org.listenbrainz.shared.model.ListenWorkerInput
import org.listenbrainz.shared.model.ResponseError
import org.listenbrainz.shared.model.dao.PendingListensDao
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.listens.ListensRepository
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.Resource

@Worker("ListenSubmissionWorker")
class ListenSubmissionWorker: AndroidWorker, KoinComponent {

    private var logger:Log = Log
    private val appPreferences: AppPreferences by inject()
    private val repository: ListensRepository by inject()
    private val pendingListensDao: PendingListensDao by inject()
    private val buildInfo: BuildInfo by inject()

    override suspend fun doWork(input: String?, env: WorkerEnvironment): WorkerResult {
        val token = appPreferences.lbAccessToken.get()
        if (token.isEmpty()) {
            logger.d("ListenBrainz User token has not been set!")
            return WorkerResult.Failure("No token")
        }
        val data = try {
            Json.decodeFromString<ListenWorkerInput>(input ?: "")
        } catch (e: SerializationException) {
            logger.e("Failed to parse worker input: ${e.message}")
            return WorkerResult.Failure("Bad input")
        }
        val inputTrack = data.track
        val inputListenType = data.listenType
        val duration: Long? = when(inputTrack.duration) {
            0L -> null
            in 1L..30_000L -> {
                logger.d("Track is too short to submit, duration: ${inputTrack.duration}ms")
                return WorkerResult.Failure("Track duration under submission threshold")
            }
            else -> inputTrack.duration
        }

        val metadata = ListenTrackMetadata(
            artist = inputTrack.artist,
            track = inputTrack.title,
            release = inputTrack.releaseName,
            additionalInfo = AdditionalInfo(
                durationMs = duration?.toInt(),
                mediaPlayer = inputTrack.pkgName?.let { repository.getPackageLabel(it) },
                submissionClient = "ListenBrainz Android",
                submissionClientVersion = buildInfo.versionName
            )
        )

        if (!metadata.isValid()) {
            logger.d("Track metadata is not valid: $metadata")
            return WorkerResult.Failure("Invalid metadata")
        }

        // Our listen to submit
        val listen = ListenSubmitBody.Payload(
            listenedAt = when (inputListenType) {
                ListenType.SINGLE -> {
                    inputTrack.timestampSeconds
                }
                else -> null
            },
            metadata = metadata
        )

        val body = ListenSubmitBody().addListens(listen)

        body.listenType = inputListenType.code

        // TODO: Inject dispatcher here and below as well.
        val response = withContext(Dispatchers.IO) {
            repository.submitListen(token, body)
        }

        return when (response.status) {
            Resource.Status.SUCCESS -> {
                if (body.listenType == ListenType.PLAYING_NOW.code) {
                    logger.d("Playing Now submitted")
                } else {
                    logger.d("Listen submitted")
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
                            logger.d("Pending listens submitted.")
                            pendingListensDao.deleteAllPendingListens()
                        }
                        else -> {
                            logger.w("Could not submit pending listens.")
                        }
                    }
                }

                WorkerResult.Success("Submitted: ${inputTrack.title}")

            }
            else -> {
                // In case of failure, we add this listen to pending list.
                if (inputListenType == ListenType.SINGLE) {
                    // We don't want to submit playing nows later.
                    if (response.error is ResponseError.BadRequest) {
                        logger.e(
                            "Submission failed, not saving listen because metadata is faulty."
                            + "\n Server response: ${response?.error?.toast}" + "\n POST Request Body: $body"
                        )
                    } else {
                        logger.e("Submission failed, listen saved.")
                        pendingListensDao.addListen(listen)
                    }
                } else {
                    // Playing now was not submitted.
                    logger.e("Could not submit playing now. Reason: " + (response.error?.toast ?: "Unknown"))
                }

                WorkerResult.Failure("Submission failed")
            }
        }
    }
}