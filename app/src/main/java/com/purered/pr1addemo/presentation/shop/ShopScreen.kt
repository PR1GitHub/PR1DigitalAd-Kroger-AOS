package com.purered.pr1addemo.presentation.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.purered.pr1addemo.R
import com.purered.pr1addemo.presentation.Dimens.MediumPadding1
import com.purered.pr1addemo.presentation.Dimens.MediumPadding2
import com.purered.pr1addemo.presentation.weeklyAd.components.ZoomableImage
import com.purered.pr1addemo.ui.theme.NewsAppTheme


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp

import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import kotlin.math.absoluteValue

@OptIn(ExperimentalComposeUiApi::class)

@Composable
fun ZoomableBoxImageContent() {
    var scale by remember {
        mutableStateOf(1f)
    }
    var offset by remember {
        mutableStateOf(Offset.Zero)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        val state = rememberTransformableState { zoomChange, panChange, rotationChange ->
            scale = (scale * zoomChange).coerceIn(1f, 5f)

            val extraWidth = (scale - 1) * constraints.maxWidth
            val extraHeight = (scale - 1) * constraints.maxHeight

            val maxX = extraWidth / 2
            val maxY = extraHeight / 2

            offset = Offset(
                x = (offset.x + scale * panChange.x).coerceIn(-maxX, maxX),
                y = (offset.y + scale * panChange.y).coerceIn(-maxY, maxY),
            )
        }
        Image(
            painter = painterResource(R.drawable.pureredlogo),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .transformable(state)
        )
    }
}

//@Composable
//fun ZoomableBoxContent(
//    content: @Composable () -> Unit
//) {
//    var scale by remember { mutableStateOf(1f) }
//    var offset by remember { mutableStateOf(Offset.Zero) }
//    var boxSize by remember { mutableStateOf(IntSize.Zero) }
//
//    val scrollState = rememberScrollState()
//
//    BoxWithConstraints(
//        modifier = Modifier
//            .fillMaxWidth()
//          //  .verticalScroll(scrollState)
//            .onGloballyPositioned { coordinates ->
//                boxSize = coordinates.size
//            }
//    ) {
//        Box(
//            modifier = Modifier
//                .graphicsLayer {
//                    scaleX = scale
//                    scaleY = scale
//                    translationX = offset.x
//                    translationY = offset.y
//                }
//                .pointerInput(Unit) {
//                    detectTransformGestures { _, pan, zoom, _ ->
//                        scale = (scale * zoom).coerceIn(1f, 5f)
//
//                        val extraWidth = (scale - 1) * boxSize.width
//                        val extraHeight = (scale - 1) * boxSize.height
//
//                        val maxX = extraWidth / 2
//                        val maxY = extraHeight / 2
//
//                        offset = Offset(
//                            x = (offset.x + scale * pan.x).coerceIn(-maxX, maxX),
//                            y = (offset.y + scale * pan.y).coerceIn(-maxY, maxY),
//                        )
//                    }
//                }
//        ) {
//            // Replace with your composable content inside the Box
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Gray)
//            ) {
//                content()
//                // Add more content here if needed
//            }
//        }
//    }
//}


@Composable
fun ZoomableBoxContent(
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
                    scale = (scale * zoom).coerceIn(1f, 5f)

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
                    detectTapGestures(
                        onDoubleTap = {
                            scale = 1f
                            offset = Offset.Zero
                        }
                    )
                }
        ) {
            // Replace with your composable content inside the Box
            content()
        }
    }
}



// Stable zoomable box content but inner child element click events conflicts
//@Composable
//fun ZoomableBoxContent(
//    content: @Composable () -> Unit
//) {
//    var scale by remember { mutableStateOf(1f) }
//    var offset by remember { mutableStateOf(Offset.Zero) }
//    var boxSize by remember { mutableStateOf(IntSize.Zero) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .pointerInput(Unit) {
//                detectTransformGestures { _, pan, zoom, _ ->
//                    scale = (scale * zoom).coerceIn(1f, 5f)
//
//                    val extraWidth = (scale - 1) * boxSize.width
//                    val extraHeight = (scale - 1) * boxSize.height
//
//                    val maxX = extraWidth / 2
//                    val maxY = extraHeight / 2
//
//                    offset += pan * scale
//
//                    offset = Offset(
//                        x = offset.x.coerceIn(-maxX.absoluteValue, maxX.absoluteValue),
//                        y = offset.y.coerceIn(-maxY.absoluteValue, maxY.absoluteValue)
//                    )
//                }
//
//            }
//            .onGloballyPositioned { coordinates ->
//                boxSize = coordinates.size
//            }
//            .graphicsLayer {
//                scaleX = scale
//                scaleY = scale
//                translationX = offset.x
//                translationY = offset.y
//            }
//            .pointerInput(Unit) {
//                detectTapGestures(
//                    onDoubleTap = {
//                        scale = 1f
//                        offset = Offset.Zero
//                    }
//                )
//            }
//    ) {
//        // Replace with your composable content inside the Box
//        content()
//    }
//}

@Composable
fun ShopScreen() {

    Column(modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center) {
        //ZoomableBoxImageContent()
//        ZoomableBoxContent(
//            content = {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(Color.Gray)
//                ) {
//                    Text(
//                        text = "Pinch to Zoom",
//                        modifier = Modifier
//                            .background(Color.Blue)
//                            .padding(16.dp)
//                    )
//                    Spacer(modifier = Modifier.height(20.dp))
//                    Text(
//                        text = "Drag to Pan",
//                        modifier = Modifier
//                            .background(Color.Green)
//                            .padding(16.dp)
//                    )
//                }
//            }
//        )
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.60f),
            painter = painterResource(id = R.drawable.pureredlogo),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )

       // ZoomableImage()
        Spacer(modifier = Modifier.height(MediumPadding1))
        Text(
            modifier = Modifier.padding(horizontal = MediumPadding2),
            text = "PureRED",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = Color.Red
        )
        Text(
            modifier = Modifier.padding(horizontal = MediumPadding2),
            text ="Demo Application",
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(id = R.color.text_medium)
        )


    }

}

@Preview
@Composable
fun PreviewShopScreen() {
    NewsAppTheme {
        ShopScreen()
    }
}