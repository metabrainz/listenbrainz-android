package org.listenbrainz.android.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** ListenBrainz Logo loading animation.
 *
 * To fit the animation, actual free space required is ( 3*[logoSize] x [logoSize] ).
 * @param logoSize Size of the logo represented as Size([logoSize],[logoSize]).
 * @param middleGap Gap between the parallelograms. Automatically adjusted with [logoSize].
 * @param sideOffset Half the total length of the outer side of the parallelogram. Automatically adjusted with [logoSize].
 * @param backgroundColor Background of the animation. Defaults to [Color.Transparent].
 * @param rightQuadColor Tint of right quadrilateral/parallelogram of the logo.
 * @param leftQuadColor Tint of left quadrilateral/parallelogram of the logo.
 * @param translateDuration Duration of translation and alpha animation.
 * @param rotateDuration Duration of rotate animation.
 */
@Preview
@Composable
fun LoadingAnimation(
    logoSize: Dp = 60.dp,
    middleGap: Dp = logoSize / 20,
    sideOffset: Dp = logoSize / 4,
    backgroundColor: Color = Color.Transparent,
    rightQuadColor: Color = Color(0xFFEA743B),
    leftQuadColor: Color = Color(0xFF353070),
    translateDuration: Int = 600,
    rotateDuration: Int = 1000
) {
    var startTranslateAndAlpha by remember { mutableStateOf(false) }
    
    val translateAnimation by animateDpAsState(
        targetValue = if (startTranslateAndAlpha) 0.dp else logoSize,
        animationSpec = tween(translateDuration)
    )
    
    val alphaAnim by animateFloatAsState(
        targetValue = if (startTranslateAndAlpha) 1f else 0f,
        animationSpec = tween(translateDuration)
    )
    
    val rotateAnim = remember { Animatable(0f) }
    
    LaunchedEffect(Unit){
        launch {
            while (true) {
                startTranslateAndAlpha = true
                delay(translateDuration.toLong())   // Wait for translate animation to finish
                rotateAnim.animateTo(    // Automatically runs blocking.
                    targetValue = 180f,
                    animationSpec = tween(rotateDuration)
                )
                startTranslateAndAlpha = false
                delay(translateDuration.toLong())   // Wait for translate animation to finish
                rotateAnim.snapTo(0f)
            }
        }
    }
    
    // Content
    Box(
        modifier = Modifier
            .height(logoSize * 3)
            .width(logoSize)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Spacer(
            modifier = Modifier
                .size(logoSize)
                .drawWithCache {
                    
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    val orangeRectPath = Path()
                    orangeRectPath.moveTo(center.x + middleGap.toPx(), 0f)
                    orangeRectPath.lineTo(center.x + middleGap.toPx(), size.height)
                    orangeRectPath.lineTo(size.width, (size.height / 2 + sideOffset.toPx()))
                    orangeRectPath.lineTo(size.width, (size.height / 2 - sideOffset.toPx()))
                    orangeRectPath.close()
                    
                    val orangeRectColor = rightQuadColor.copy(alpha = alphaAnim)
                    
                    val purpleRectPath = Path()
                    purpleRectPath.moveTo(center.x - middleGap.toPx(), 0f)
                    purpleRectPath.lineTo(center.x - middleGap.toPx(), size.height)
                    purpleRectPath.lineTo(0f, (size.height / 2 + sideOffset.toPx()))
                    purpleRectPath.lineTo(0f, (size.height / 2 - sideOffset.toPx()))
                    purpleRectPath.close()
                    
                    val purpleRectColor = leftQuadColor.copy(alpha = alphaAnim)
                    
                    onDrawBehind {
                        
                        rotate(rotateAnim.value) {
                            
                            translate(top = -translateAnimation.toPx()) {
                                drawPath(orangeRectPath, orangeRectColor)
                            }
                            
                            translate(top = translateAnimation.toPx()) {
                                drawPath(purpleRectPath, purpleRectColor)
                            }
                        }
                    }
                }
        )
    }
}
