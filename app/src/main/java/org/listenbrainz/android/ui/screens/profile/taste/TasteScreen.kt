package org.listenbrainz.android.ui.screens.profile.taste

import LovedHated
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.viewmodel.ProfileViewModel

@Composable
fun TasteScreen(
    viewModel: ProfileViewModel,
    uiState: ProfileUiState,
) {
    val lovedHatedState: MutableState<LovedHated> = remember { mutableStateOf(LovedHated.loved) }
    LazyColumn {
        item {
            Row {
                ElevatedSuggestionChip(onClick = { lovedHatedState.value = LovedHated.loved }, label = { Text("Loved") })
                ElevatedSuggestionChip(onClick = { lovedHatedState.value = LovedHated.hated }, label = { Text("Hated") })
            }
        }
    }
}