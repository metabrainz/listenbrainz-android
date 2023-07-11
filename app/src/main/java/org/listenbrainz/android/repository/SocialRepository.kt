package org.listenbrainz.android.repository

import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.model.SimilarUserData
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.util.Resource

interface SocialRepository {
    
    suspend fun getFollowers(username: String) : Resource<SocialData>
    
    suspend fun getFollowing(username: String) : Resource<SocialData>
    
    suspend fun followUser(username: String) : Resource<SocialResponse>
    
    suspend fun unfollowUser(username: String) : Resource<SocialResponse>
    
    suspend fun getSimilarUsers(username: String) : Resource<SimilarUserData>
    
    suspend fun searchUser(username: String) : Resource<SearchResult>
    
}