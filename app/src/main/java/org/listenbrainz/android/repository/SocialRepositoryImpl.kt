package org.listenbrainz.android.repository

import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.model.SimilarUserData
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.service.SocialService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.authHeader
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepositoryImpl @Inject constructor(private val service: SocialService) : SocialRepository {

    /** @return Network Failure, User DNE, Success.*/
    override suspend fun getFollowers(username: String) : Resource<SocialData> =
        runCatching {
            val response = service.getFollowersData(username = username)
            returnResponse(response)
        
        }.getOrDefault(Resource.failure())
    
    
    /** @return Network Failure, User DNE, Success.*/
    override suspend fun getFollowing(username: String) : Resource<SocialData> =
        runCatching {
            val response = service.getFollowingData(username = username)
            returnResponse(response)
            
        }.getOrDefault(Resource.failure())
    
    
    /** @return Network Failure, User DNE, User already followed, Success.*/
    override suspend fun followUser(username: String, accessToken: String): Resource<SocialResponse> =
        runCatching {
            val response = service.followUser(username = username, authHeader = authHeader(accessToken))
            returnResponse(response)
            
        }.getOrDefault(Resource.failure())
    
    
    /** Apparently server does not return 400 in case a user is not followed already.
     * @return Network Failure, User DNE, Success.
     */
    override suspend fun unfollowUser(username: String, accessToken: String): Resource<SocialResponse> =
        runCatching {
            val response = service.unfollowUser(username = username, authHeader = authHeader(accessToken))
            returnResponse(response)
            
        }.getOrDefault(Resource.failure())
    
    
    /** @return Network Failure, User DNE, Success. */
    override suspend fun getSimilarUsers(username: String): Resource<SimilarUserData> =
        runCatching {
            val response = service.getSimilarUsersData(username = username)
            returnResponse(response)
        
        }.getOrDefault(Resource.failure())
    
    
    /** @return Network Failure, Success. */
    override suspend fun searchUser(username: String): Resource<SearchResult> =
        runCatching {
            val response = service.searchUser(username = username)
            returnResponse(response)
        
        }.getOrDefault(Resource.failure())
    
    
    
    private fun <T> returnResponse(response: Response<T>) : Resource<T> {
        return if (response.isSuccessful) {
            Resource.success(response.body()!!)
        } else {
            /** Conditions on entering this block:
              *  1) User does not exist (Code 404)
              *  2) User already followed (Code 400)
              *
              *  Access [error] in response body to know what's up.
             */
            Resource.failure(response.body())
        }
    }
}