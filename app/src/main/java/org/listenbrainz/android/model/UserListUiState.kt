package org.listenbrainz.android.model

/** This Ui state may be used to show default user stats that may or may not be mutable.
 * For example, isFollowed variable is mutable as user may change its state. Where as there can be other
 * immutable properties as well. Mutable properties preferably should be included using flow operators.*/
data class UserListUiState(
    val userList: List<User> = emptyList(),
    val isFollowedList: List<Boolean> = emptyList()
)