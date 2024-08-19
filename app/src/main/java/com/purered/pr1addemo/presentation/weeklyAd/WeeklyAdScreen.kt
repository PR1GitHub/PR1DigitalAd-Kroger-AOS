package com.purered.pr1addemo.presentation.weeklyAd

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.purered.pr1addemo.data.repository.AdPage
import com.purered.pr1addemo.data.repository.WeeklyAd
import com.purered.pr1addemo.presentation.weeklyAd.components.AdPageView
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize


import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.purered.pr1addemo.presentation.shop.ZoomableBoxContent
import com.purered.pr1addemo.presentation.weeklyAd.components.ZoomableImage
import kotlin.math.max
import kotlin.math.min


import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.purered.pr1digitaladclassic.ApiEnv


import com.purered.pr1digitaladclassic.DigitalAd
import com.purered.pr1digitaladclassic.SpotClickPayload



// Function to constrain zoom level
fun constrainZoom(zoom: Float, min: Float = 1f, max: Float = 5f): Float {
    return max(min, min(zoom, max))
}

@Composable
fun PinchZoomableBox(
    content: @Composable () -> Unit,

) {
    var modifier: Modifier = Modifier
    var minScale: Float = 1f
    var maxScale: Float = 5f
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastScale by remember { mutableStateOf(1f) }
    var lastOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Blue) // Replace with your content background
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = constrainZoom(lastScale * zoom, minScale, maxScale)

                    // Calculate new offset based on pan and scale
                    val newOffset = lastOffset + pan

                    // Ensure the content doesn't move out of the box
                    val maxX = (size.width * (newScale - 1)) / 2
                    val maxY = (size.height * (newScale - 1)) / 2

                    offset = Offset(
                        x = newOffset.x.coerceIn(-maxX, maxX),
                        y = newOffset.y.coerceIn(-maxY, maxY)
                    )

                    // Update the scale
                    scale = newScale
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        // Reset last scale and offset
                        lastScale = scale
                        lastOffset = offset
                    }
                )
            }
    ) {
        content()
    }
}







@Composable
fun WeeklyAdScreen(
    modifier: Modifier = Modifier) {


    var hotSpotData: SpotClickPayload? by remember { mutableStateOf(null) }

                DigitalAd(
                    adId = "8d7d7bfd-9dd1-4738-b98f-cb1ec10e3026",
                    location =  "01800775",
                    apiEnv = ApiEnv.PROD,
                    apiKey = "bqwwosbzrzcvffztxzyczieljzsahmkp",
                    onHotSpotClick = {  payload:SpotClickPayload ->

                    hotSpotData = payload

                    if (payload.itemType == "promo") {
                       // Handle content type Creative
                    }

                    if (payload.itemType == "offer") {
                        // Handle content type Offer
                    }
                })











        if (hotSpotData != null) {
            AlertDialog(
                modifier = Modifier.wrapContentHeight(),
                onDismissRequest = { hotSpotData = null },
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth() ,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically // Center align the content vertically
                    ) {
                        Text(
                            text = "Offer Details",
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { hotSpotData = null }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {

                        if(hotSpotData != null) {
                            Text(text = hotSpotData.toString())
                        }
                    }
                },
                confirmButton = { }
            )
        }


//    val eventId = 12032 // Replace with dynamic value
//
//    val mode = "public"
//    val weeklyAdViewModel: WeeklyAdsViewModel = viewModel()
//    val viewState by weeklyAdViewModel.weeklyAdState
//
//
//    LaunchedEffect(Unit) {
//        weeklyAdViewModel.fetchWeeklyAd(eventId, mode)
//    }
//    Box(modifier = Modifier.fillMaxSize()) {
//        when {
//            viewState.loading -> {
//                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//            }
//            viewState.error != null -> {
//                Column(modifier = Modifier.align(Alignment.Center)) {
//                    Text(text = "Something went wrong. Please try again.")
//                    TextButton(onClick = { weeklyAdViewModel.reloadWeeklyAd() }) {
//                        Text("Try Again")
//                    }
//                }
//
//            }
//            else -> {
//                if(viewState.WeeklyAd != null)
//                WeeklyAdItem(ad = viewState.WeeklyAd!!)
//            }
//        }
//    }


}

@Composable
fun WeeklyAdItem(ad: WeeklyAd) {

    var pages:List<AdPage> = emptyList();
    if(ad.pages.isNotEmpty()){
        pages = pages + ad.pages[0]
    }
    Box(modifier = Modifier
        .fillMaxSize()

        .padding(2.dp)
    ) {


        ZoomableBoxContent {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) { // Add verticalScroll modifier
                ad.pages.forEachIndexed { index, adPage ->
                    // Pass each AdPage to a separate AdPageView
                    if(adPage.fileURL.isNotEmpty() == true)
                    AdPageView(
                        adPage = adPage,
                        modifier = Modifier.padding(2.dp),
                        eventId = ad.eventId,
                        onAdClick = {},
                        key = index
                    )
                }
            }
        }
    }

}



@Composable
fun WeeklyAdPage(adPage: AdPage) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Text(text = adPage.fileName, style = MaterialTheme.typography.headlineMedium)

    }
}
