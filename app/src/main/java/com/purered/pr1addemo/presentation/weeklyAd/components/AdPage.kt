package com.purered.pr1addemo.presentation.weeklyAd.components


import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.purered.pr1addemo.data.repository.AdPage
import coil.size.Size as CoilSize
import android.graphics.Paint.Style
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Popup
import com.purered.pr1addemo.presentation.Dimens
import com.purered.pr1addemo.presentation.common.shimmerEffect
import kotlin.math.floor
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.purered.pr1addemo.data.repository.PageContent
import com.purered.pr1addemo.presentation.weeklyAd.WeeklyAdItem
import com.google.gson.Gson
import com.purered.pr1addemo.data.repository.weeklyAdService
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import coil.compose.SubcomposeAsyncImage
import com.purered.pr1addemo.R

import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import com.purered.pr1addemo.data.repository.OfferDetails
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ZoomableImage() {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    var initialScale by remember { mutableStateOf(1f) }
    var initialOffsetX by remember { mutableStateOf(0f) }
    var initialOffsetY by remember { mutableStateOf(0f) }

    Image(
        painter = painterResource(id = R.drawable.pureredlogo),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (initialScale * zoom).coerceIn(1f, 5f)
                    offsetX = initialOffsetX + pan.x
                    offsetY = initialOffsetY + pan.y
                }
            }
            .pointerInteropFilter {
                when (it.actionMasked) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        initialScale = scale
                        initialOffsetX = offsetX
                        initialOffsetY = offsetY
                    }
                }
                true
            }
    )
}


fun convertHotMapsToDisplaySize(
    hotMaps: List<MapArea>,
    originalImageWidth: Float,
    originalImageHeight: Float,
    displayWidth: Float,
    displayHeight: Float
): List<MapArea> {

    if (originalImageWidth == 0f || originalImageHeight == 0f || displayWidth == 0f || displayHeight == 0f) {
        // Return empty list if any of the dimensions are zero
        return emptyList()
    }
    try {

        val scaleX = if (originalImageWidth != 0f) displayWidth / originalImageWidth else 1f
        val scaleY = if (originalImageHeight != 0f) displayHeight / originalImageHeight else 1f

        val convertedHotMaps = hotMaps.map { mapArea ->


            mapArea.copy(
                x1 = mapArea.x1 * scaleX,
                y1 = mapArea.y1 * scaleY,
                x2 = mapArea.x2 * scaleX,
                y2 = mapArea.y2 * scaleY
            )

            /* MapArea(
                 x1 = mapArea.x1 * scaleX,
                 y1 = mapArea.y1 * scaleY,
                 x2 = mapArea.x2 * scaleX,
                 y2 = mapArea.y2 * scaleY,
                 lineWidth = mapArea.lineWidth,
                 color = mapArea.color,
                 isSelected = mapArea.isSelected,
                 hasAttached = mapArea.hasAttached
             )*/


        }

        println(convertedHotMaps)
        return convertedHotMaps
    } catch (e: Exception) {
        println(e)
        return emptyList()
    }


}

fun getScreenWidth(context: Context): Int {
    val displayMetrics = DisplayMetrics()
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

@Composable
fun pixelsToDp(pixels: Float): Dp {
    val density = LocalDensity.current
    return with(density) { pixels.toDp() }
}


@Composable
fun AdPageView(
    modifier: Modifier = Modifier,
    adPage: AdPage? = null,
    eventId: Int,

    onAdClick: (() -> Unit)? = null,
    key: Int
) {
    var imageWidth by remember { mutableStateOf(0f) }
    var imageHeight by remember { mutableStateOf(0f) }
    var imageState by remember { mutableStateOf("loading") }
    var displaySize by remember { mutableStateOf(Size.Zero) }
    var dpiimageWidth by remember { mutableStateOf(0.dp) }
    var dpiimageHeight by remember { mutableStateOf(0.dp) }
    //var selectedMapArea by remember { mutableStateOf(MapArea(0f, 0f, 0f, 0f, 0, "", false, false)) }

    var offerDetails by remember { mutableStateOf<OfferDetails?>(null) }
    val gson = Gson()
    val coroutineScope = rememberCoroutineScope()
    var convertedHotMaps by remember { mutableStateOf(emptyList<MapArea>()) }
    var isOfferLoading by remember { mutableStateOf(false) }


    val context = LocalContext.current

    val fileUrl = adPage?.compressedFileURL;

    // fileUrl = "https://omskrogerqa.blob.core.windows.net/83a8f217-a180-4f34-acc0-51709f2ff862/31b344df-992c-4a42-9bcb-3b80c9478e68.jpg";
    //fileUrl ="https://omskrogerqa.blob.core.windows.net/anonymous/4f6f14f0-6aa9-4c7d-89ad-1ed95cbead9f.jpg"


    dpiimageWidth = pixelsToDp(displaySize.width)
    dpiimageHeight = pixelsToDp(displaySize.height)

    val wDp = pixelsToDp(displaySize.width)
    val hDp = pixelsToDp(displaySize.height)
    LaunchedEffect(fileUrl) {


        val adPageId = adPage?.eventPageId;


        if (adPageId != null) {

            try {
                val adPageData: AdPage = weeklyAdService.getPageDetails(eventId, adPageId, "public")


                if (adPageData.contents.isNotEmpty()) {

                    val request = ImageRequest.Builder(context)
                        .data(fileUrl)
                        .size(CoilSize.ORIGINAL) // Ensure original dimensions are fetched
                        .build()
                    val result = (context.imageLoader.execute(request) as SuccessResult).drawable
                    imageWidth = result.intrinsicWidth.toFloat()
                    imageHeight = result.intrinsicHeight.toFloat()

                    /* println("Original Image Size: width: $imageWidth, height: $imageHeight")
                     println("Display Size: width: ${displaySize.width}, height: ${displaySize.height}")
                     println("Display Size inDPI : width: ${dpiimageWidth.value}, height: ${dpiimageHeight.value}")*/



                    var pagehotMaps: List<MapArea> = emptyList()

                    adPageData?.contents?.forEachIndexed() { index, pageContent ->
                        val mapArea = gson.fromJson(pageContent.mapConfig, MapArea::class.java)
                        pagehotMaps = pagehotMaps + mapArea
                    }

                    if (imageWidth > 0f && imageHeight > 0f && dpiimageWidth > 0.dp && dpiimageHeight > 0.dp) {

                        convertedHotMaps = convertHotMapsToDisplaySize(
                            hotMaps = pagehotMaps,
                            originalImageWidth = imageWidth,
                            originalImageHeight = imageHeight,
                            displayWidth = dpiimageWidth.value,
                            displayHeight = dpiimageHeight.value
                        )
                    }

                }

            } catch (e: Exception) {
                println(e)
            }


        }


    }


    var isPopupVisible by remember { mutableStateOf(false) }


    var loadOfferDetails: suspend (selectedMapArea:MapArea) -> Unit =  { selectedMapArea ->

        try {

            if (selectedMapArea?.content?.offerVersionProductGroupId != null) {
                val offerDetailsList = weeklyAdService.getOfferDetails(
                    eventId,
                    selectedMapArea!!.content!!.offerVersionProductGroupId.toInt()
                );
                println("offerDetailsList ********")
                println(offerDetailsList)
                if (offerDetailsList.isNotEmpty()) {

                  //  offerDetails = offerDetailsList[0]
                    println(offerDetailsList)
                } else {

                }
            }

        } catch (
            e: Exception
        ) {
            Toast.makeText(
                context,
                "",
                Toast.LENGTH_LONG
            ).show()

            println("Error fetching offer details ${e.message}")
        }finally {
            isOfferLoading = false
        }
    }

   // val painter = rememberAsyncImagePainter(model = fileUrl)

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutCoordinates ->
                displaySize = layoutCoordinates.size.toSize()
                // println("Display Size: width: ${displaySize.width}, height: ${displaySize.height}")
            }
    ) {



        if(imageState == "loading"){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
                    .padding(horizontal = Dimens.MediumPadding1)
                    .shimmerEffect()
            )
        }

//        SubcomposeAsyncImage(
//            model = fileUrl,
//            contentDescription = null,
//            loading = {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(440.dp)
//                        .padding(horizontal = Dimens.MediumPadding1)
//                        .shimmerEffect()
//                )
//            },
//
//            error = { BasicText(text = "Error loading image") },
//
//            contentScale = ContentScale.Fit,
//            modifier = Modifier
//                .fillMaxWidth()
//        )
        if(imageState == "error"){
            BasicText(text = "Error loading Page")
        }

        AsyncImage(
            model = fileUrl,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            onLoading = {
             imageState = "loading"
            },
            modifier = Modifier.fillMaxSize(),
            onError = {
                // Handle error
                      imageState = "error"
            },
            onSuccess = { image ->
                imageState = "success"
                // Draw the image
                // Draw the hot maps on top of the image
                // DrawHotMaps(hotMaps = convertedHotMaps, width = displaySize.width, height = displaySize.height)
            }
        )
        if (imageState== "success" && convertedHotMaps.isNotEmpty()) {
            DrawHotMaps(
                hotMaps = convertedHotMaps,
                width = displaySize.width,
                height = displaySize.height,
                pageKey = key,
                onMapAreaClick = {
                     it ->



                    if (it.contentType?.lowercase() == "creative") {
                        Toast.makeText(
                            context,
                            "Clicked on Creative spot will be redirected to ${it.content?.webUrl}",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        println("selectedMapArea  isPopupVisible  ----- ")
                        isOfferLoading = true
                        coroutineScope.launch {
                            loadOfferDetails.invoke(it)
                        }



                        //isPopupVisible = true
                    }

                }
            )
        }

        if(isOfferLoading){
            Box(
                modifier = Modifier
                    .width(wDp)
                    .height(hDp)
                    .fillMaxSize() // Fill the entire screen
                    .background(Color.Black.copy(alpha = 0.6f))  // Set the background color
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)  // Center the progress indicator
                )
            }
        }

//{ offerDetails = null }
        if (offerDetails != null) {
            AlertDialog(
                modifier = Modifier.wrapContentHeight(),
                onDismissRequest = { offerDetails = null },
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
                        IconButton(onClick = { offerDetails = null }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
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

                     if(offerDetails != null) {
                         MapAreaContentView(
                             modifier = Modifier,
                             offerDetails = offerDetails!!,
                             eventId = eventId
                         ) {
                             offerDetails = null
                         }
                     }
                    }
                },
                confirmButton = { }
            )
        }


        /*


                    when {
                        viewState.loading -> {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Blue
                            )
                        }
                        viewState.error != null -> {


                        }
                        else -> {
                            if(viewState.AdPage?.contents?.isNotEmpty() == true)
                                DrawHotMaps(
                                    hotMaps = convertedHotMaps,
                                    width = displaySize.width,
                                    height = displaySize.height,
                                    pageKey = key,
                                    onMapAreaClick = {
                                        isPopupVisible = true
                                    }
                                )
                        }
                    }
        */

//            if (viewState.loading || isLoading) {
//                LinearProgressIndicator(
//                    modifier = Modifier.fillMaxWidth(),
//                    color = Color.Blue
//                )
//            } else {
//
//
//
//                }

    }

} // Box


@Composable
private fun DrawHotMaps(
    hotMaps: List<MapArea>,
    width: Float,
    height: Float,
    pageKey: Int,
    onMapAreaClick: ((MapArea) -> Unit)
) {
    if (hotMaps.isEmpty()) {
        return
    }

    val context = LocalContext.current
    val wDp = pixelsToDp(width)
    val hDp = pixelsToDp(height)

    Box(
        modifier = Modifier
            .width(wDp)
            .height(hDp)
            //.border(2.dp, Color.Black)
            .background(Color.Transparent)
    ) {
        hotMaps.forEach { mapArea ->
            val left = floor(mapArea.x1).dp
            val top = floor(mapArea.y1).dp
            val right = floor(mapArea.x2).dp
            val bottom = floor(mapArea.y2).dp
//Color.LightGray.copy(alpha = 0.4f)
            Box(
                modifier = Modifier
                    .offset(x = left, y = top)
                    .width(right - left)
                    .height(bottom - top)
                    .background(Color.Transparent)
                    .border(1.dp, Color.Transparent)
                    .clickable {
                        onMapAreaClick(mapArea)
                    }
            ) {
                // Optionally, you can add content to each Box here, like labels or icons
//                Text(
//                    text = "Area",
//                    color = Color.White,
//                    fontSize = 14.sp,
//                    modifier = Modifier.align(Alignment.Center)
//                )
            }
        }
    }
}

/*

@Composable
private fun DrawHotMaps(
    hotMaps: List<MapArea>,
    width: Float,
    height: Float,
    pageKey: Int,
    onMapAreaClick: ((MapArea) -> Unit)

) {
    if (hotMaps.isEmpty()) {
        return
    }
    val context = LocalContext.current
    val wDp = pixelsToDp(width)
    val hDp = pixelsToDp(height)

    Canvas(modifier = Modifier
        .width(wDp)
        .height(hDp)
        .border(2.dp, Color.Black)
        // .background(Color(0xFF00FF00).copy(alpha = 0.6f))
        .pointerInput(Unit) {
            detectTapGestures { offset ->
                val x = offset.x
                val y = offset.y
                val density = context.resources.displayMetrics.density // Get device pixel density
                hotMaps.forEach { mapArea ->
                    val left = floor(mapArea.x1).dp.toPx()
                    val top = floor(mapArea.y1).dp.toPx()
                    val right = floor(mapArea.x2).dp.toPx()
                    val bottom = floor(mapArea.y2).dp.toPx()

                    if (x >= left && x <= right && y >= top && y <= bottom) {
                        // Touch event occurred within this drawRect
                        // Trigger corresponding action
                        // println("Rectangle clicked: $mapArea")

                        onMapAreaClick(mapArea)

                    }
                }
            }
        }

    ) {
        val paint = Paint().apply {
            color = Color.White.toArgb() // Text color
            textSize = 30f // Text size
            isAntiAlias = true // Enable anti-aliasing for smoother text
            // Add more styling properties as needed
        }


        val density = context.resources.displayMetrics.density // Get device pixel density
        hotMaps.forEach { mapArea ->

            val left = floor(mapArea.x1).dp.toPx()
            val top = floor(mapArea.y1).dp.toPx()
            val right = floor(mapArea.x2).dp.toPx()
            val bottom = floor(mapArea.y2).dp.toPx()


            drawRect(
                color = Color.LightGray,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 4.dp.toPx())

            )


        }

    }


}
*/


// Data classes to match the provided JSON structure


data class HotMaps(
    val height: Int,
    val width: Int,
    val maps: List<MapArea>
)

data class MapAreaContent(
    val altText: String,
    val appUrl: String,
    val event: String,
    val id: String,
    val webUrl: String,
    val bodyCopy: String,
    val headline: String,
    val imageURL: String,
    val index: String,
    val offerVersionProductGroupId: String,
)


data class MapArea(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val lineWidth: Int,
    val color: String,
    val isSelected: Boolean,
    val hasAttached: Boolean,
    val contentId: String? = null,
    val contentType: String? = null, //"Creative" or "Offer"
    val eventPageContentId: String? = null,
    var content: MapAreaContent? = null

)


data class BoxData(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

