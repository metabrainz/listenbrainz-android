package org.listenbrainz.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

/**
 * Data class representing a chip item with label and optional icon.
 *
 * @param id Unique identifier for the chip
 * @param label Display text for the chip
 * @param icon Optional painter for the icon to display
 */
data class ChipItem<T>(
    val label: String,
    val id: T,
    val icon: Painter? = null
)

/**
 * A reusable component for displaying a horizontal scrollable row of selection chips.
 * Each chip represents a selectable option with visual feedback for the selected state.
 *
 * @param items List of ChipItem objects to display as chips
 * @param selectedItemId The ID of the currently selected item
 * @param onItemSelected Callback invoked when a chip is clicked with the selected item's ID
 * @param modifier Modifier to be applied to the container
 */
@Composable
fun <T> SelectionChipBar(
    items: List<ChipItem<T>>,
    selectedItemId: T,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = ListenBrainzTheme.paddings.horizontal),
    onItemSelected: (ChipItem<T>) -> Unit,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items.forEach { item ->
            item {
                val isSelected = selectedItemId == item.id

                SelectionChip(
                    isSelected = isSelected,
                    label = item.label,
                    icon = item.icon,
                    onClick = { onItemSelected(item) }
                )

                Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.horizontal))
            }
        }
    }
}

/**
 * Individual selection chip component with consistent styling.
 */
@Composable
private fun SelectionChip(
    isSelected: Boolean,
    label: String,
    icon: Painter?,
    onClick: () -> Unit
) {
    val chipColor = when (isSelected) {
        true -> ListenBrainzTheme.colorScheme.followerChipUnselected
        false -> ListenBrainzTheme.colorScheme.followerChipSelected
    }

    ElevatedSuggestionChip(
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    color = chipColor,
                    style = ListenBrainzTheme.textStyles.chips
                )
                if (icon != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.height(15.dp),
                        tint = chipColor
                    )
                }
            }
        },
        shape = ListenBrainzTheme.shapes.signatureChips,
        border = when (isSelected) {
            true -> null
            false -> BorderStroke(1.dp, ListenBrainzTheme.colorScheme.lbSignature)
        },
        colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
            if (isSelected) {
                ListenBrainzTheme.colorScheme.followerChipSelected
            } else {
                ListenBrainzTheme.colorScheme.followerChipUnselected
            }
        ),
    )
}
