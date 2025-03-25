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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.ui.input.pointer.*
import kotlinx.coroutines.*

@Composable
fun ZoomableBoxContent(
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var isTapDetected by remember { mutableStateOf(false) } // 🔥 NEW: Track taps


    var hotMapViewModel:HotMapViewModel = viewModel()

    val coroutineScope = rememberCoroutineScope() // ✅ Define scope here
    var isTouching by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isTapDetected = true // 🔥 Prevent zoom when a tap is detected
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (!isTouching) {
                        isTouching = true
                        hotMapViewModel.onZoomStart()
                        Log.d("ZoomTracker", "Zoom started")
                    }
                    Log.d("DrawHotMaps", "detectTransformGestures ONNNNN..!!!")
                    if (!isTapDetected) {
                        Log.d("DrawHotMaps", "ZOOOOOMINGGGG..!!!")
                        scale = (scale * zoom).coerceIn(1f, 5f)

                        val extraWidth = (scale - 1) * boxSize.width
                        val extraHeight = (scale - 1) * boxSize.height

                        val maxX = extraWidth / 2
                        val maxY = extraHeight / 2

                        // ✅ Adjusted pan factor for better movement speed
                        val adjustedPan = pan * 0.8f  // Reduce dampening effect, making panning easier
                        offset += adjustedPan

                        // ✅ Allow full movement without over-restricting edges
                        offset = Offset(
                            x = offset.x.coerceIn(-maxX * 1.2f, maxX * 1.2f),  // Give extra space for better reachability
                            y = offset.y.coerceIn(-maxY * 1.2f, maxY * 1.2f)
                        )
                    }


                    isTapDetected = false // Reset tap detection after gesture ends
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.none { it.pressed } && isTouching) {
                            isTouching = false
                            coroutineScope.launch {
                                delay(300)  // Delay before ending zoom
                                hotMapViewModel.onZoomEnd()
                                Log.d("ZoomTracker", "Zoom ended after delay")
                            }
                        }
                    }
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
        ) {
            // Replace with your composable content inside the Box
            content()
        }
    }
}