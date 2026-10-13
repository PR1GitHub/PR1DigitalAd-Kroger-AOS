package com.purered.pr1digitaladclassic

import android.util.Log
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
/*

@Composable
fun ZoomableBoxContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    enableZoomButtons: Boolean,
    zoomButtonOffset: Int
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var isTapDetected by remember { mutableStateOf(false) }

    val hotMapViewModel: HotMapViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    var isTouching by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(Color.Transparent)
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { isTapDetected = true }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (!isTouching) {
                        isTouching = true
                        hotMapViewModel.onZoomStart()
                        Log.d("ZoomTracker", "Zoom started")
                    }
                    if (!isTapDetected) {
                        scale = (scale * zoom).coerceIn(1f, 5f)

                        val extraWidth = (scale - 1) * boxSize.width
                        val extraHeight = (scale - 1) * boxSize.height
                        val maxX = extraWidth / 2
                        val maxY = extraHeight / 2

                        val adjustedPan = pan * 0.8f
                        offset += adjustedPan

                        offset = Offset(
                            x = offset.x.coerceIn(-maxX, maxX),
                            y = offset.y.coerceIn(-maxY, maxY)
                        )
                    }
                    isTapDetected = false
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.none { it.pressed } && isTouching) {
                            isTouching = false
                            coroutineScope.launch {
                                delay(300.milliseconds)
                                hotMapViewModel.onZoomEnd()
                            }
                        }
                    }
                }
            }
            .onGloballyPositioned { coordinates -> boxSize = coordinates.size }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        ) {
            content()
        }

        if (enableZoomButtons) {
            //  Zoom Controls (+ and - buttons)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = zoomButtonOffset.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(
                    modifier = Modifier
                        .padding(4.dp),

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    FloatingActionButton(
                        onClick = { scale = (scale + 1f).coerceAtMost(5f) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(
                                    topStart = 10.dp,
                                    topEnd = 10.dp,
                                    bottomEnd = 0.dp,
                                    bottomStart = 0.dp
                                ) // Custom corner radii
                            ),
                        containerColor = Color.Transparent, // No default color
                        elevation = FloatingActionButtonDefaults.elevation(0.dp) // Remove shadow
                    ) {
                        Text(
                            text = "+",  // Use "+" or "-" directly
                            color = Color.White,
                            fontSize = 30.sp // Adjust size as needed
                        )
                    }


                    FloatingActionButton(
                        onClick = {
                            scale = (scale - 1f).coerceAtLeast(1f)
                            if (scale == 1f) {
                                offset = Offset.Zero
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .offset(y = (-2).dp)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 0.dp,
                                    bottomEnd = 10.dp,
                                    bottomStart = 10.dp
                                )
                            ), // Transparent button background
                        containerColor = Color.Transparent,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Text(
                            text = "_",  // Use "+" or "‾-_" directly
                            color = Color.White,
                            fontSize = 30.sp, // Adjust size as needed
                            modifier = Modifier.offset(y = (-13).dp) // Adjust vertical position
                        )
                    }
                }
            }
        }
    }
}
*/

private const val MinZoom = 1f
private const val MaxZoom = 5f

/**
 * Zoom state for [ZoomableBoxContent], hoisted so the owner can reset it.
 *
 * The One Ad wraps its whole pager in one ZoomableBoxContent, so this single
 * scale/offset applies to whichever page is showing. Before this was hoisted the
 * zoom survived page changes: the page the user left stayed zoomed and the new
 * page arrived zoomed (CLNTIMP-1595). The pager now calls [animateReset] when the
 * page changes; nothing resets while the user stays on a page.
 */
@Stable
class ZoomableState internal constructor() {
    var scale by mutableFloatStateOf(MinZoom)
        internal set
    var offset by mutableStateOf(Offset.Zero)
        internal set

    val isZoomed: Boolean
        get() = scale > MinZoom || offset != Offset.Zero

    /** Snap straight back to the default view. */
    fun reset() {
        scale = MinZoom
        offset = Offset.Zero
    }

    /**
     * Ease back to the default view. A no-op at 1x, so callers can invoke it on
     * every page change without producing a visible jump when nothing is zoomed.
     */
    suspend fun animateReset(durationMillis: Int = ZoomResetDurationMillis) {
        if (!isZoomed) return
        val startScale = scale
        val startOffset = offset
        animate(0f, 1f, animationSpec = tween(durationMillis)) { fraction, _ ->
            scale = lerp(startScale, MinZoom, fraction)
            offset = lerp(startOffset, Offset.Zero, fraction)
        }
        reset()
    }

    companion object {
        const val ZoomResetDurationMillis = 200
    }
}

@Composable
fun rememberZoomableState(): ZoomableState = remember { ZoomableState() }

@Composable
fun ZoomableBoxContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    enableZoomButtons: Boolean,
    zoomButtonOffset: Int,
    zoomState: ZoomableState = rememberZoomableState()
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var isTapDetected by remember { mutableStateOf(false) }

    val hotMapViewModel: HotMapViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    var isTouching by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isTapDetected = true
                    }
                )
            }
            .pointerInput(Unit) {
                // detectTransformGestures consumed EVERY drag past touch slop, including
                // single-finger vertical drags at 1x zoom - so a host screen embedding
                // the ad in its own scrollable column could never scroll (client
                // landscape layouts). This loop claims a gesture only when it is a real
                // zoom interaction: two or more pointers down, or any drag while
                // already zoomed in. Unclaimed events flow to the pager and to the
                // host's own scrolling untouched.
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var zoomAccumulator = 1f
                    var panAccumulator = Offset.Zero
                    var pastTouchSlop = false

                    do {
                        val event = awaitPointerEvent()
                        val pointersDown = event.changes.count { it.pressed }
                        val engaged = pointersDown > 1 || zoomState.scale > MinZoom

                        if (engaged) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            if (!pastTouchSlop) {
                                zoomAccumulator *= zoomChange
                                panAccumulator += panChange
                                val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                val zoomMotion = abs(1 - zoomAccumulator) * centroidSize
                                if (zoomMotion > viewConfiguration.touchSlop ||
                                    panAccumulator.getDistance() > viewConfiguration.touchSlop
                                ) {
                                    pastTouchSlop = true
                                }
                            }

                            if (pastTouchSlop) {
                                if (!isTouching) {
                                    isTouching = true
                                    hotMapViewModel.onZoomStart()
                                    Log.d("ZoomTracker", "Zoom started")
                                }

                                zoomState.scale = (zoomState.scale * zoomChange).coerceIn(MinZoom, MaxZoom)

                                val extraWidth = (zoomState.scale - 1) * boxSize.width
                                val extraHeight = (zoomState.scale - 1) * boxSize.height
                                val maxX = extraWidth / 2
                                val maxY = extraHeight / 2

                                zoomState.offset += panChange * 0.8f
                                zoomState.offset = Offset(
                                    x = zoomState.offset.x.coerceIn(-maxX, maxX),
                                    y = zoomState.offset.y.coerceIn(-maxY, maxY)
                                )

                                event.changes.forEach {
                                    if (it.positionChanged()) it.consume()
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()

                        if (
                            event.changes.none { it.pressed } &&
                            isTouching
                        ) {
                            isTouching = false

                            coroutineScope.launch {
                                delay(300.milliseconds)
                                hotMapViewModel.onZoomEnd()
                            }
                        }
                    }
                }
            }
            .onGloballyPositioned {
                boxSize = it.size
            }
    ) {

        // Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = zoomState.scale
                    scaleY = zoomState.scale
                    translationX = zoomState.offset.x
                    translationY = zoomState.offset.y
                }
        ) {
            content()
        }

        if (enableZoomButtons) {

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = zoomButtonOffset.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {

                Column(
                    modifier = Modifier.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    FloatingActionButton(
                        onClick = {
                            zoomState.scale = (zoomState.scale + 1f)
                                .coerceAtMost(MaxZoom)
                        },
                        // The glyph is the whole button, so without this a screen reader
                        // announced these two controls as "plus" and "underscore".
                        modifier = Modifier
                            .size(48.dp)
                            .semantics { contentDescription = "Zoom in" },
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Text(
                            text = "+",
                            color = Color.White,
                            fontSize = 30.sp,
                            modifier = Modifier.clearAndSetSemantics { }
                        )
                    }

                    FloatingActionButton(
                        onClick = {
                            zoomState.scale = (zoomState.scale - 1f)
                                .coerceAtLeast(MinZoom)

                            if (zoomState.scale == MinZoom) {
                                zoomState.offset = Offset.Zero
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .offset(y = (-2).dp)
                            .semantics { contentDescription = "Zoom out" },
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        // Was an underscore nudged up 13.dp to look like a minus sign: it
                        // read as "underscore" and drifted out of the button as the user's
                        // font scale grew. A real minus glyph centres itself.
                        Text(
                            text = "\u2212",
                            color = Color.White,
                            fontSize = 30.sp,
                            modifier = Modifier.clearAndSetSemantics { }
                        )
                    }
                }
            }
        }
    }
}