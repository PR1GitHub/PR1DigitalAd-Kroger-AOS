package com.purered.pr1digitaladclassic

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
fun ZoomableBoxContent4(
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Handle zoom scaling
                    scale = (scale * zoom).coerceIn(1f, 5f)

                    // Handle panning with constraints
                    val extraWidth = (scale - 1) * boxSize.width
                    val extraHeight = (scale - 1) * boxSize.height

                    val maxX = extraWidth / 2
                    val maxY = extraHeight / 2

                    offset += pan * scale

                    offset = Offset(
                        x = offset.x.coerceIn(-maxX.absoluteValue, maxX.absoluteValue),
                        y = offset.y.coerceIn(-maxY.absoluteValue, maxY.absoluteValue)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Compose gestures: pass zoom gestures to parent and handle taps here
                    detectTransformGestures { _, _, _, _ -> /* Pass through zoom gestures */ }
                }
        ) {
            // The `content` parameter represents the tappable hotmaps
            content()
        }
    }
}