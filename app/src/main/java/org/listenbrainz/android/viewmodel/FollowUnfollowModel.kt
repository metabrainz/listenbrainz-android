package org.listenbrainz.android.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.User
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.util.Resource

abstract class FollowUnfollowModel<UiState>(
    private val repository: SocialRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
): BaseViewModel<UiState>() {
    
    /**
     * @param [invertUiState] **should** check for cancellation internally.*/
    suspend fun optimisticallyFollowUser(
        user: User,
        index: Int,
        invertUiState: (index: Int) -> Unit
    ) {
        /** Determines if user is followed or not.*/
        fun userIsAlreadyFollowed(error: ResponseError?): Boolean {
            return error == ResponseError.BAD_REQUEST &&
                    error.actualResponse?.contains("already following") == true
        }
        
        invertUiState(index)
        
        val result = withContext(ioDispatcher) {
            repository.followUser(user.username)
        }
        when (result.status) {
            Resource.Status.FAILED -> {
                emitError(result.error)
                
                if (userIsAlreadyFollowed(result.error)){
                    // We won't toggle back follow state if user is already followed.
                    return
                }
                
                invertUiState(index)
            }
            else -> Unit
        }
    }
    
    /**
     * @param [invertUiState] **should** check for cancellation internally.*/
    suspend fun optimisticallyUnfollowUser(
        user: User,
        index: Int,
        invertUiState: (index: Int) -> Unit
    ) {
        
        invertUiState(index)
        
        val result = withContext(ioDispatcher) {
            repository.unfollowUser(user.username)
        }
        return when (result.status) {
            Resource.Status.FAILED -> {
                // Since same response is given by server even if user is unfollowed or not, we
                // won't do anything here.
                invertUiState(index)
                emitError(result.error)
            }
            else -> Unit
        }
    }
}