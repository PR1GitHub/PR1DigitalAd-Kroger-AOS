package com.purered.pr1digitaladclassic

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ZoomableBoxContent(
    content: @Composable () -> Unit,
    enableZoomButtons: Boolean,
    zoomButtonOffset: Int
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var isTapDetected by remember { mutableStateOf(false) }

    val hotMapViewModel: HotMapViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    var isTouching by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                            x = offset.x.coerceIn(-maxX * 1.2f, maxX * 1.2f),
                            y = offset.y.coerceIn(-maxY * 1.2f, maxY * 1.2f)
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
                .fillMaxSize()
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
                    .fillMaxSize()
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
