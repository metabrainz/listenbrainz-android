package org.listenbrainz.android.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import java.text.DecimalFormat

/**
 * Select an appropriate cardBackground that goes with the given theme.
 * @param similarity should lie between 0f and 1f.
 */
@Composable
fun SimilarUserCard(
    uiModeIsDark: Boolean = onScreenUiModeIsDark(),
    cardBackGround: Color = MaterialTheme.colorScheme.background,
    index: Int,
    userName: String,
    similarity: Float
){
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = cardBackGround,
        shape = RoundedCornerShape(5.dp),
        shadowElevation = 5.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ranking Text
                Text(
                    text = "#${index + 1}",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                        .copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiModeIsDark) MaterialTheme.colorScheme.onSurface else lb_purple
                        )
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Username Text
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyLarge
                        .copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiModeIsDark) MaterialTheme.colorScheme.onSurface else lb_purple,
                            lineHeight = 14.sp
                        ) ,
                )
            }
            
            Row(modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.5f)
                .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Similarity Bar
                LinearProgressIndicator(
                    progress = similarity,
                    modifier = Modifier
                        .clip(CircleShape)
                        .width(70.dp),
                    trackColor = if (uiModeIsDark) Color.White else Color.LightGray,
                    color = when (similarity) {
                        in 0.70..1.00 -> if (uiModeIsDark) Color(0xFF7B65FF) else Color(0xFF382F6F)
                        in 0.30..0.69 -> Color(0xFFF57542)
                        else -> Color(0xFFD03E43)
                    }
                )
                
                // Similarity Text
                Text(
                    text = "${DecimalFormat("#.#").format(similarity*10)}/10",
                    color = if (uiModeIsDark) Color.White else Color.Black.copy(alpha = 0.4f),
                    fontFamily = FontFamily(Font(R.font.roboto_regular)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(5.dp)
                        .width(45.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun Preview(){
    ListenBrainzTheme {
        SimilarUserCard(index = 0, userName = "jasje", similarity = 0.80f)
    }
}
