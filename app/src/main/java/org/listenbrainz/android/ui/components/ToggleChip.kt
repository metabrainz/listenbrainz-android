package org.listenbrainz.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple_night

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToggleChips(
    modifier: Modifier = Modifier,
    currentPageStateProvider: () -> Int,
    chips: List<String>,
    icons: List<Int?>? = null,
    scope: CoroutineScope = rememberCoroutineScope(),
    onClick: suspend (Int) -> Unit
) {
    // Row container for the toggle chips
    FlowRow(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        ListenBrainzTheme.colorScheme.background, Color.Transparent
                    )
                )
            )
    ) {
        // Spacer to add padding at the start of the row
        Spacer(
            modifier = Modifier.width(ListenBrainzTheme.paddings.chipsHorizontal / 2)
        )
        // Loop through the chips list to create each chip
        repeat(chips.size) { position ->
            ElevatedSuggestionChip(
                modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.chipsHorizontal),
                colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                    if (currentPageStateProvider() == position) {
                        ListenBrainzTheme.colorScheme.followerChipSelected
                    } else {
                        ListenBrainzTheme.colorScheme.followerChipUnselected
                    }
                ),
                shape = ListenBrainzTheme.shapes.listenCardSmall,
                elevation = SuggestionChipDefaults.elevatedSuggestionChipElevation(elevation = 4.dp),
                label = {
                    // Text label for the chip
                    Text(
                        text = chips[position],
                        style = ListenBrainzTheme.textStyles.chips,
                        color = if (currentPageStateProvider() == position) ListenBrainzTheme.colorScheme.followerChipUnselected
                        else ListenBrainzTheme.colorScheme.followerChipSelected,
                    )
                    // Optional icon for the chip
                    if (icons?.get(position) != null) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            painterResource(icons[position]!!),
                            contentDescription = "Icon of toggle",
                            tint = if (currentPageStateProvider() == position) ListenBrainzTheme.colorScheme.followerChipUnselected
                            else ListenBrainzTheme.colorScheme.followerChipSelected
                        )
                    }
                },
                // Border for the chip when not selected
                border = when (currentPageStateProvider() == position) {
                    true -> null
                    false -> BorderStroke(1.dp, lb_purple_night)
                },
                // Handle chip click event
                onClick = { scope.launch { onClick(position) } }
            )
        }
    }
}