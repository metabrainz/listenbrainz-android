package org.listenbrainz.android.presentation.features.yim.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun YimNavButton(goBack: Boolean = false ,onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (goBack) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.onBackground, CircleShape)
                .size(50.dp),
            tint = MaterialTheme.colorScheme.background,
            contentDescription = "Move to next page"
        )
    }
}