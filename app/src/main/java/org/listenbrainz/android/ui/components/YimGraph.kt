package org.listenbrainz.android.ui.components

import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
fun YimGraph (yearListens : List<Pair<String , Int>>) {

    val modelProducer = remember {
        CartesianChartModelProducer()
    }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            while(isActive){
                modelProducer.runTransaction { columnSeries {
                    series(x = yearListens.map { it.first.toInt() }, y = yearListens.map { it.second })
                } }
            }

        }
    }

    CartesianChartHost(
        modifier = Modifier
            .padding(start = 11.dp, end = 11.dp)
            .height(250.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFe0e5de)),
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        color = Color(0xFFe36b3c),
                        thickness = 25.dp,
                    )
                ),
                spacing = 1.dp
            ),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                label = rememberTextComponent (
                    ellipsize = TextUtils.TruncateAt.MARQUEE,
                    textSize = 11.sp
                ),
                guideline = null,
                valueFormatter = CartesianValueFormatter { value, chartValues, verticalAxisPosition ->
                       if(value.toInt() % 5 == 0){
                           value.toString()
                       }
                    else{
                        ""
                       }
                }
            ),
        ),
        modelProducer = modelProducer
    )

}