package com.purered.pr1digitaladclassic

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
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
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import kotlin.math.absoluteValue

// ZoomState to store zoom-related data

fun List<PointerInputChange>.calculateZoom(): Float {
    if (size < 2) return 1f

    val first = this[0].position
    val second = this[1].position

    val previousFirst = this[0].previousPosition
    val previousSecond = this[1].previousPosition

    val distance = first.getDistanceTo(second)
    val previousDistance = previousFirst.getDistanceTo(previousSecond)

    return if (previousDistance != 0f) distance / previousDistance else 1f
}

fun Offset.getDistanceTo(other: Offset): Float {
    return ((x - other.x).square() + (y - other.y).square()).sqrt()
}

fun Float.square() = this * this
fun Float.sqrt() = kotlin.math.sqrt(this)


///* -- Working Code --
@Composable
fun ZoomableBoxContent11(
    zoomState: ZoomState,
    content: @Composable () -> Unit
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var isGestureInProgress by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    var previousCentroid: Offset? = null
                    var previousScale = 1f

                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes

                        if (changes.isNotEmpty()) {
                            val pointerCount = changes.size

                            // Multi-finger zoom/pan handling
                            if (pointerCount > 1) {
                                val centroid = changes.map { it.position }
                                    .reduce { acc, offset -> acc + offset } / pointerCount.toFloat()
                                val scaleChange = changes.calculateZoom()

                                if (!isGestureInProgress) {
                                    isGestureInProgress = true
                                    zoomState.isZooming = true
                                }

                                if (previousCentroid != null) {
                                    val panChange = centroid - previousCentroid

                                    // Apply zoom
                                    zoomState.scale = (zoomState.scale * scaleChange).coerceIn(1f, 5f)

                                    // Apply panning
                                    val extraWidth = (zoomState.scale - 1) * boxSize.width
                                    val extraHeight = (zoomState.scale - 1) * boxSize.height

                                    val maxX = extraWidth / 2
                                    val maxY = extraHeight / 2

                                    zoomState.offset += panChange * zoomState.scale
                                    zoomState.offset = Offset(
                                        x = zoomState.offset.x.coerceIn(-maxX.absoluteValue, maxX.absoluteValue),
                                        y = zoomState.offset.y.coerceIn(-maxY.absoluteValue, maxY.absoluteValue)
                                    )
                                }

                                previousCentroid = centroid
                                previousScale = scaleChange
                            }

                            // Single-finger tap handling
                            if (pointerCount == 1) {
                                isGestureInProgress = false
                                zoomState.isZooming = false
                            }

                            changes.forEach { it.consume() }
                        }
                    }
                }
            }
            .onGloballyPositioned { coordinates ->
                boxSize = coordinates.size // Capture the size of the Box
            }
            .graphicsLayer {
                scaleX = zoomState.scale
                scaleY = zoomState.scale
                translationX = zoomState.offset.x
                translationY = zoomState.offset.y
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            content()
        }
    }
}
//*/


/* -- Initial code --
@Composable
fun ZoomableBoxContent11(
    zoomState: ZoomState,
    content: @Composable () -> Unit
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var isGestureInProgress by remember { mutableStateOf(false) } // Track gesture state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->

                    //Log.i("HOTMAP > ZOOM","Initiated zoom")
                    if (!isGestureInProgress) {
                        isGestureInProgress = true
                        zoomState.isZooming = true
                        //Log.i("HOTMAP > ZOOM","Initiated !isGestureInProgress")
                    }


                    zoomState.scale = (zoomState.scale * zoom).coerceIn(1f, 5f)

                    val extraWidth = (zoomState.scale - 1) * boxSize.width
                    val extraHeight = (zoomState.scale - 1) * boxSize.height

                    val maxX = extraWidth / 2
                    val maxY = extraHeight / 2

                    zoomState.offset += pan * zoomState.scale

                    zoomState.offset = Offset(
                        x = zoomState.offset.x.coerceIn(-maxX.absoluteValue, maxX.absoluteValue),
                        y = zoomState.offset.y.coerceIn(-maxY.absoluteValue, maxY.absoluteValue)
                    )

                    //Log.i("HOTMAP > ZOOM","zoom ended")
                }
            }
//            .pointerInput(Unit) {
//                detectTapGestures(
//                    onPress = {
//                        if (isGestureInProgress) {
//                            isGestureInProgress = false
//                            zoomState.isZooming = false
//                        }
//                    }
//                )
//            }
            .onGloballyPositioned { coordinates ->
                boxSize = coordinates.size
            }
            .graphicsLayer {
                scaleX = zoomState.scale
                scaleY = zoomState.scale
                translationX = zoomState.offset.x
                translationY = zoomState.offset.y
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    Log.i("HOTMAP > detectTapGestures","detectTapGestures started")
                    detectTapGestures(
                        onDoubleTap = {
                            zoomState.scale = 1f
                            zoomState.offset = Offset.Zero
                        }
                    )
                }
        ) {
            content()
        }
    }
}
 */