package org.listenbrainz.shared.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val BlobStart = Color(0xFFDFA426)
private val BlobEnd = Color(0xFF5D5998)

private fun blob(
    name: String,
    width: Float,
    height: Float,
    pathData: String,
    gradientStart: Offset,
    gradientEnd: Offset,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = width.dp,
    defaultHeight = height.dp,
    viewportWidth = width,
    viewportHeight = height,
).addPath(
    pathData = PathParser().parsePathString(pathData).toNodes(),
    fill = Brush.linearGradient(
        colors = listOf(BlobStart, BlobEnd),
        start = gradientStart,
        end = gradientEnd,
    ),
).build()

val OnboardBlob1: ImageVector by lazy {
    blob(
        name = "OnboardBlob1",
        width = 183f,
        height = 204f,
        pathData = "M4.36,91.32C-16,27.06 46.94,-13.37 110.94,4.8C156.65,8.11 212.23,65.4 163.6,81.57C114.97,97.73 197.28,108.97 143.62,178.8C89.95,248.62 24.72,155.58 4.36,91.32Z",
        gradientStart = Offset(170.62f, -3.2f),
        gradientEnd = Offset(34.76f, 163.71f),
    )
}

val OnboardBlob2: ImageVector by lazy {
    blob(
        name = "OnboardBlob2",
        width = 95f,
        height = 70f,
        pathData = "M91.003,33.833C105.822,65.124 75.443,70.924 30.357,69.488C-16.326,72.835 0.041,15.376 25.827,5.083C51.613,-5.216 76.184,2.543 91.003,33.84L91.003,33.833Z",
        gradientStart = Offset(21.54f, 74.328f),
        gradientEnd = Offset(71.486f, 8.875f),
    )
}

val OnboardBlob3: ImageVector by lazy {
    blob(
        name = "OnboardBlob3",
        width = 77f,
        height = 95f,
        pathData = "M7.449,50.261C25.975,106.282 53.706,109.053 74.485,60.329C95.276,11.613 -30.344,-40.415 7.449,50.261Z",
        gradientStart = Offset(9.941f, 76.747f),
        gradientEnd = Offset(52.817f, 12.063f),
    )
}

@Preview
@Composable
private fun OnboardBlobsPreview() {
    Row {
        Image(OnboardBlob1, contentDescription = null)
        Image(OnboardBlob2, contentDescription = null)
        Image(OnboardBlob3, contentDescription = null)
    }
}
