package org.listenbrainz.android.ui.screens.search

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.SearchUiState
import org.listenbrainz.android.model.User
import org.listenbrainz.android.model.UserListUiState
import org.listenbrainz.android.ui.components.FollowButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.SearchViewModel

@Composable
fun UserSearchScreen(
    isActive: Boolean,
    viewModel: SearchViewModel = hiltViewModel(),
    goToUserPage: (String) -> Unit,
    deactivate: () -> Unit
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val uiState by viewModel.uiState.collectAsState()
        
        SearchScreen(
            uiState = uiState,
            onDismiss = {
                deactivate()
                viewModel.clearUi()
            },
            onQueryChange = viewModel::updateQueryFlow,
            onClear = viewModel::clearUi,
            onErrorShown = viewModel::clearErrorFlow,
            placeholderText = "Search Users"
        ) {
            UserList(
                uiState = uiState,
                onFollowClick = viewModel::toggleFollowStatus,
                goToUserPage = goToUserPage
            )
        }
    }
}

@Composable
private fun UserList(
    uiState: SearchUiState<UserListUiState>,
    /** Must return if the operation was successful.*/
    onFollowClick: (User, Int) -> Unit,
    goToUserPage: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(ListenBrainzTheme.paddings.lazyListAdjacent)
    ) {
        itemsIndexed(uiState.result.userList) { index, user ->
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ListenBrainzTheme.paddings.lazyListAdjacent)
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Profile",
                            tint = ListenBrainzTheme.colorScheme.hint
                        )
                        
                        Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.coverArtAndTextGap))
                        
                        Text(
                            text = user.username,
                            color = ListenBrainzTheme.colorScheme.text,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                goToUserPage(user.username)
                            }
                        )
                    }
                    
                    FollowButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        isFollowedState = uiState.result.isFollowedList[index]
                    ) {
                        onFollowClick(uiState.result.userList[index], index)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(uiMode = UI_MODE_NIGHT_NO)
@Composable
private fun SearchScreenPreview() {
    ListenBrainzTheme {
        val uiState = remember {
            SearchUiState(
                query = "Jasjeet",
                result = UserListUiState(
                    listOf(User("Jasjeet"),
                        User("JasjeetTest"),
                        User("Jako")
                    ),
                    listOf(false, true, true)
                ),
                error = ResponseError.DOES_NOT_EXIST
            )
        }

        SearchScreen(
            uiState = uiState,
            onDismiss = {},
            onQueryChange = {},
            onClear = {},
            onErrorShown = {},
            placeholderText = "Search Users"
        ) {
            UserList(
                uiState = uiState,
                { _, _ -> },
                {}
            )
        }
    }
}