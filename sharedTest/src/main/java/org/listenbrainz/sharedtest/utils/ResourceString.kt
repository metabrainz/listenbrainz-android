package org.listenbrainz.sharedtest.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ResourceString {
    
    val auth_header_not_found_error by lazy {
        EntityTestUtils.loadResourceAsString("auth_header_not_found_error.json")
    }
    
    val cannot_follow_self_error by lazy {
        EntityTestUtils.loadResourceAsString("cannot_follow_self_error.json")
    }
    
    val already_following_error by lazy {
        EntityTestUtils.loadResourceAsString("already_following_error.json")
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
    
    val my_feed_page_1 by lazy {
        EntityTestUtils.loadResourceAsString("my_feed_page_1.json")
    }
    
    val my_feed_page_2 by lazy {
        EntityTestUtils.loadResourceAsString("my_feed_page_2.json")
    }
    
    val follow_listens_page_1 by lazy {
        EntityTestUtils.loadResourceAsString("follow_listens_page_1.json")
    }
    
    val follow_listens_page_2 by lazy {
        EntityTestUtils.loadResourceAsString("follow_listens_page_2.json")
    }
    
    val similar_listens_page_1 by lazy {
        EntityTestUtils.loadResourceAsString("similar_listens_page_1.json")
    }
    
    val similar_listens_page_2 by lazy {
        EntityTestUtils.loadResourceAsString("similar_listens_page_2.json")
    }
    
    val rate_limiting_error by lazy {
        "{\"code\":429,\"error\":\"\"}"
    }
    
    val unknown_error by lazy {
        "{\"code\":400,\"error\":\"Wow new error!\"}"
    }
    
    val yim_data by lazy {
        EntityTestUtils.loadResourceAsString("yim_data.json")
    }
    
    fun <T> String.toClass(): T {
        return Gson().fromJson(this, object: TypeToken<T>() {}.type)
    }
    
}