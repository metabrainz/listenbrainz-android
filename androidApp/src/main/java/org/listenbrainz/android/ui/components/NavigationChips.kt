package org.listenbrainz.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun NavigationChips(
    currentPageStateProvider: () -> Int,
    chips: List<String>,
    scope: CoroutineScope = rememberCoroutineScope(),
    onClick: suspend (Int) -> Unit
){
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(
                Brush.Companion.verticalGradient(
                    listOf(
                        ListenBrainzTheme.colorScheme.background,
                        Color.Companion.Transparent
                    )
                )
            )
    ) {
        Spacer(
            modifier = Modifier.Companion.width(ListenBrainzTheme.paddings.chipsHorizontal / 2)
        )
        repeat(chips.size) { position ->
            ElevatedSuggestionChip(
                modifier = Modifier.Companion.padding(ListenBrainzTheme.paddings.chipsHorizontal),
                colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                    if (currentPageStateProvider() == position) {
                        ListenBrainzTheme.colorScheme.chipSelected
                    } else {
                        ListenBrainzTheme.colorScheme.chipUnselected
                    }
                ),
                shape = ListenBrainzTheme.shapes.chips,
                elevation = SuggestionChipDefaults.elevatedSuggestionChipElevation(elevation = 4.dp),
                label = {
                    Text(
                        text = chips[position],
                        style = ListenBrainzTheme.textStyles.chips,
                        color = ListenBrainzTheme.colorScheme.text,
                    )
                },
                onClick = { scope.launch { onClick(position) } }
            )
        }
    }

}