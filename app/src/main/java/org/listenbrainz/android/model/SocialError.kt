package org.listenbrainz.android.model

enum class SocialError(override val genericToast: String, override var actualResponse: String? = null): ResponseError {
    
    USER_NOT_FOUND(genericToast = "User not found."),     // "User Some_User_That_Does_Not_Exist not found"
    
    ALREADY_FOLLOWING(genericToast = "Already following user."),     // "Jasjeet is already following user someotheruser"
    
    CANNOT_FOLLOW_SELF(genericToast = "Whoops, cannot follow yourself.");  // "Whoops, cannot follow yourself."
    
}