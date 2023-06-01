package org.listenbrainz.sharedtest.utils

object ResourceString {
    
    val auth_header_not_found_error by lazy {
        EntityTestUtils.loadResourceAsString("auth_header_not_found_error.json")
    }
    
    val cannot_follow_self_error by lazy {
        EntityTestUtils.loadResourceAsString("cannot_follow_self_error.json")
    }
    
    val follow_error_response by lazy {
        EntityTestUtils.loadResourceAsString("follow_error_response.json")
    }
    
    val followers_response by lazy {
        EntityTestUtils.loadResourceAsString("followers_response.json")
    }
    
    val following_response by lazy {
        EntityTestUtils.loadResourceAsString("following_response.json")
    }
    
    val search_response by lazy {
        EntityTestUtils.loadResourceAsString("search_response.json")
    }
    
    val similar_users_response by lazy {
        EntityTestUtils.loadResourceAsString("similar_users_response.json")
    }
    
    val status_ok by lazy {
        EntityTestUtils.loadResourceAsString("status_ok.json")
    }
    
    val user_does_not_exist_error by lazy {
        EntityTestUtils.loadResourceAsString("user_does_not_exist_error.json")
    }
    
    val yim_data by lazy {
        EntityTestUtils.loadResourceAsString("yim_data.json")
    }
    
    
}