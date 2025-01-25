package com.purered.pr1digitaladclassic

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import kotlin.math.absoluteValue

@Composable
fun ZoomableBoxContent2(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    offset: Offset,
    onOffsetChange: (Offset) -> Unit,
    content: @Composable () -> Unit
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                    onScaleChange(newScale)

                    val extraWidth = (newScale - 1) * boxSize.width
                    val extraHeight = (newScale - 1) * boxSize.height
                    val maxX = extraWidth / 2
                    val maxY = extraHeight / 2

                    val newOffset = offset + pan * newScale
                    onOffsetChange(
                        Offset(
                            x = newOffset.x.coerceIn(-maxX.absoluteValue, maxX.absoluteValue),
                            y = newOffset.y.coerceIn(-maxY.absoluteValue, maxY.absoluteValue)
                        )
                    )
                }
            }
            .onGloballyPositioned { coordinates ->
                boxSize = coordinates.size
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
    ) {
        content()
    }
}