package org.listenbrainz.android.ui.screens.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.model.Event
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.viewmodel.FeedViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EventScreen(
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val events = viewModel.eventsFlow.collectAsState().value
    LaunchedEffect(key1 = events) {
        viewModel.fetchEvents("20244d07-534f-4eff-b4d4-930878889970")
        d("Events: $events")
    }

    Column {
        Text(
            text = "Events",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
        LazyColumn {
            items(events) { event ->
                EventItem(event = event)
            }
        }
    }
}

@Composable
fun EventItem(event: Event) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Event, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = event.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.body1)
                event.time?.let { Text(text = it, style = MaterialTheme.typography.body2) }
                Text(text = event.type, style = MaterialTheme.typography.body2)
                event.lifeSpan.let {
                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val beginDate = it.begin?.let { it1 -> formatter.parse(it1) }
                    val endDate = it.end?.let { it1 -> formatter.parse(it1) }
                    Text(text = "From ${beginDate?.let { it1 -> formatter.format(it1) }} to ${endDate?.let { it1 ->
                        formatter.format(
                            it1
                        )
                    }}", style = MaterialTheme.typography.body2)
                }
            }
        }
    }
}

