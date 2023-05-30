package org.listenbrainz.android.ui.screens.explore

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.android.viewmodel.ProfileViewModel

@Composable
fun ExploreScreen(
    context : Context = LocalContext.current,
    viewModel: ProfileViewModel = hiltViewModel()
) {

    val loginStatus = viewModel.getLoginStatusFlow()
        .collectAsState(initial = viewModel.appPreferences.loginStatus, context = Dispatchers.Default)
        .value

    when(loginStatus) {
        STATUS_LOGGED_IN -> {

        }
        else -> {

        }
    }
}