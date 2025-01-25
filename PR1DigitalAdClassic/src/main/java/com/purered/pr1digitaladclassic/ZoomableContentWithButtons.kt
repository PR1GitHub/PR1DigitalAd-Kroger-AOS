package com.purered.pr1digitaladclassic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import kotlin.math.absoluteValue

@Composable
fun ZoomableContentWithButtons(
    scale: Float, // Current zoom level passed from the parent
    onScaleChanged: (Float) -> Unit, // Callback to update zoom level in the parent
    content: @Composable () -> Unit // Composable content to display inside
) {
    var offset by remember { mutableStateOf(Offset.Zero) } // Pan offset
    var boxSize by remember { mutableStateOf(IntSize.Zero) } // Size of the box for bounds calculation

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Update the scale (zoom level)
                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                    onScaleChanged(newScale) // Update zoom level via callback

                    // Update the pan offset based on the current scale
                    val extraWidth = (newScale - 1) * boxSize.width
                    val extraHeight = (newScale - 1) * boxSize.height

                    val maxX = extraWidth / 2
                    val maxY = extraHeight / 2

                    offset += pan * newScale
                    offset = Offset(
                        x = offset.x.coerceIn(-maxX.absoluteValue, maxX.absoluteValue),
                        y = offset.y.coerceIn(-maxY.absoluteValue, maxY.absoluteValue)
                    )
                }
            }
            .onGloballyPositioned { coordinates ->
                boxSize = coordinates.size // Get the size of the container
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
                    detectTapGestures(
                        onDoubleTap = {
                            // Reset scale and offset on double-tap
                            onScaleChanged(1f)
                            offset = Offset.Zero
                        }
                    )
                }
        ) {
            content() // Render the composable content
        }

        // Fixed zoom in/out buttons
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(50))
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Zoom in button
                TextButton(
                    onClick = {
                        val newScale = (scale + 0.5f).coerceIn(1f, 5f)
                        onScaleChanged(newScale)
                    }
                ) {
                    Text("+", style = MaterialTheme.typography.bodyLarge)
                }

                // Divider between buttons
                Divider(color = Color.White, thickness = 1.dp)

                // Zoom out button
                TextButton(
                    onClick = {
                        val newScale = (scale - 0.5f).coerceIn(1f, 5f)
                        onScaleChanged(newScale)
                    }
                ) {
                    Text("-", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}