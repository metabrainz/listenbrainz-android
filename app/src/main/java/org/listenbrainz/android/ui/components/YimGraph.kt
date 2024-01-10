package org.listenbrainz.android.ui.components

import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@Composable
fun YimGraph (yearListens : List<Pair<String , Int>>) {

    val chartEntries: MutableList<ChartEntry> = remember { mutableListOf() }

    for (i in 1..yearListens.size) {
        chartEntries.add(
            entryOf(
                yearListens[i - 1].first.toInt().toFloat(),
                yearListens[i - 1].second
            )
        )
    }
    val chartEntryModelProducer = ChartEntryModelProducer(chartEntries)


    Chart(
        modifier = Modifier
            .padding(start = 11.dp, end = 11.dp)
            .height(250.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFe0e5de)),
        chart = columnChart(
            spacing = 1.dp,
            columns = List(yearListens.size) {
                LineComponent(
                    color = 0xFFe36b3c.toInt(),
                    thicknessDp = 25f,
                )
            },
        ),
        chartModelProducer = chartEntryModelProducer,
        startAxis = rememberStartAxis(
            valueFormatter = { value, _ ->
                value.toInt().toString()
            }
        ),
        bottomAxis = rememberBottomAxis(
            label = textComponent {
                this.ellipsize = TextUtils.TruncateAt.MARQUEE
                this.textSizeSp = 11f
            },
            guideline = null,
            valueFormatter = { value, _ ->
                if (value.toInt() % 5 == 0) {
                    value.toInt().toString()
                } else {
                    ""
                }
            },
        ),


        )
}