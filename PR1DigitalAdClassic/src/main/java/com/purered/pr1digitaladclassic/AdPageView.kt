package com.purered.pr1digitaladclassic

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.gson.Gson
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.toSize
import coil.imageLoader

import kotlin.math.floor

@Composable
internal fun AdPageView(
    modifier: Modifier = Modifier,
    adPage: AdPage? = null,
    adId:String,
    location:String,
    onHotSpotClick: ( payload:SpotClickPayload) -> Unit,
    key: Int
) {
    var imageWidth by remember { mutableStateOf(0f) }
    var imageHeight by remember { mutableStateOf(0f) }
    var imageState by remember { mutableStateOf("loading") }
    var displaySize by remember { mutableStateOf(Size.Zero) }
    var dpiimageWidth by remember { mutableStateOf(0.dp) }
    var dpiimageHeight by remember { mutableStateOf(0.dp) }
    //var offerDetails by remember { mutableStateOf<OfferDetails?>(null) }
    val gson = Gson()
    val coroutineScope = rememberCoroutineScope()
    var convertedHotMaps by remember { mutableStateOf(emptyList<MapArea>()) }
    var isOfferLoading by remember { mutableStateOf(false) }



    val context = LocalContext.current

    val fileUrl = adPage?.compressedFileURL;



    dpiimageWidth = pixelsToDp(displaySize.width)
    dpiimageHeight = pixelsToDp(displaySize.height)

    val wDp = pixelsToDp(displaySize.width)
    val hDp = pixelsToDp(displaySize.height)


    LaunchedEffect(fileUrl) {


        val adPageId = adPage?.eventPageId;


        if (adPageId != null) {

            try {


                val adPageData: AdPage = weeklyAdService.getPageDetails(adId=adId, pageId = adPageId, location = location)


                if (adPageData.contents.isNotEmpty()) {

                    val request = ImageRequest.Builder(context)
                        .data(fileUrl)
                        .size(coil.size.Size.ORIGINAL) // Ensure original dimensions are fetched
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

                }else{
                    println(" Noooo Hot Maps")
                }

            } catch (e: Exception) {
                println("PAGE DETAILES Loading Failed")
                println(e)
            }


        }else{

        }


    }



    val onHotMapClickHandler : suspend (selectedMapArea:MapArea) -> Unit = { selectedMapArea ->



        try {



            if (selectedMapArea.contentType?.lowercase() == "creative") {


                var idVal = "0";

                try {
                    if(selectedMapArea.contentId != null) {
                        idVal = selectedMapArea.contentId.toString()
                    }
                } catch (e: NumberFormatException) {
                    println("Error: The string is not a valid integer")
                }

                var webURLVal="";
                var appURLVal="";
                var patVal = "";
                var peVal="";





                if( selectedMapArea.content != null){
                    webURLVal= selectedMapArea.content!!.webUrl
                    appURLVal= selectedMapArea.content!!.appUrl
                    patVal = selectedMapArea.content!!.altText
                    peVal = selectedMapArea.content!!.event
                }


                val payload = SpotClickPayload(
                    itemType = "promo",
                    id = idVal,
                    headline = "",
                    bodyCopy = "",
                    imageURL = "",
                    pricingHTML = "",
                    pricingText = "",
                    upc = "",
                    startDate = "",
                    endDate = "",
                    category = "",
                    disclaimer = "",
                    webURL = webURLVal,
                    appURL = appURLVal,
                    promoAltText = patVal,
                    promoEventName = peVal
                )



                onHotSpotClick(payload)
            }else{
                isOfferLoading = true
                if (selectedMapArea.content?.offerVersionProductGroupId != null) {
                    val offerDetailsList = weeklyAdService.getOfferDetails(
                        adId,
                        selectedMapArea!!.content!!.offerVersionProductGroupId,
                        location = location
                    );


                    if (offerDetailsList != null) {

                        val resOfferDetails:OfferDetails = offerDetailsList
                        val payload = SpotClickPayload(
                            itemType = "offer",
                            id = resOfferDetails.id ?: "0",
                            headline = resOfferDetails.headline ?: "",
                            bodyCopy = resOfferDetails.bodyCopy ?: "",
                            imageURL = resOfferDetails.imageURL ?: "",
                            pricingHTML = resOfferDetails.pricingHTML ?: "",
                            pricingText = resOfferDetails.pricingText ?: "",
                            upc = resOfferDetails.upc?.toIntOrNull()?.toString() ?: "",
                            startDate = resOfferDetails.startDate ?: "",
                            endDate = resOfferDetails.endDate ?: "",
                            category = resOfferDetails.category ?: "",
                            disclaimer = resOfferDetails.disclaimer ?: "",
                            webURL = resOfferDetails.webURL ?: "",
                            appURL = resOfferDetails.appURL ?: ""
                        )

                        onHotSpotClick(payload)
                    }
                }
            }


        }catch (e: Exception) {
            println("Error fetching offer details ${e.message}")
        }
        finally {
            isOfferLoading = false
        }


    }



//    val loadOfferDetails: suspend (selectedMapArea:MapArea) -> Unit =  { selectedMapArea ->
//
//        try {
//
//            if (selectedMapArea?.content?.offerVersionProductGroupId != null) {
//                val offerDetailsList = weeklyAdService.getOfferDetails(
//                    eventId,
//                    selectedMapArea!!.content!!.offerVersionProductGroupId.toInt()
//                );
//                if (offerDetailsList.isNotEmpty()) {
//                    offerDetails = offerDetailsList[0]
//                }
//            }
//
//        } catch (
//            e: Exception
//        ) {
//            Toast.makeText(
//                context,
//                "",
//                Toast.LENGTH_LONG
//            ).show()
//
//            println("Error fetching offer details ${e.message}")
//        }finally {
//            isOfferLoading = false
//        }
//    }

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
                imageState = "error"
            },
            onSuccess = { image ->
                imageState = "success"
            }
        )
        if (imageState== "success" && convertedHotMaps.isNotEmpty()) {

            //  println(" DrawHotMaps DrawHotMaps DrawHotMaps DrawHotMaps")


            DrawHotMaps(
                hotMaps = convertedHotMaps,
                width = displaySize.width,
                height = displaySize.height,
                pageKey = key,
                onMapAreaClick = {
                        it ->
                    coroutineScope.launch {
                        onHotMapClickHandler.invoke(it)
                    }

//                    if (it.contentType?.lowercase() == "creative") {
//                        Toast.makeText(
//                            context,
//                            "Clicked on Creative spot will be redirected to ${it.content?.webUrl}",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    } else {
//                        isOfferLoading = true
//                        coroutineScope.launch {
//                            loadOfferDetails.invoke(it)
//                        }
//
//
//
//                        //isPopupVisible = true
//                    }

                }
            )
        }

        if(isOfferLoading){
            Box(
                modifier = androidx.compose.ui.Modifier
                    .width(wDp)
                    .height(hDp)
                    .fillMaxSize() // Fill the entire screen
                    .background(Color.Black.copy(alpha = 0.6f))  // Set the background color
            ) {
                CircularProgressIndicator(
                    progress = 0.85F,
                    modifier = Modifier.align(Alignment.Center)  // Center the progress indicator
                )
            }
        }

//{ offerDetails = null }
//        if (offerDetails != null) {
//            AlertDialog(
//                modifier = Modifier.wrapContentHeight(),
//                onDismissRequest = { offerDetails = null },
//                title = {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth() ,
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically // Center align the content vertically
//                    ) {
//                        Text(
//                            text = "Offer Details",
//                            style = MaterialTheme.typography.titleMedium
//                        )
//                        IconButton(onClick = { offerDetails = null }) {
//                            Icon(
//                                imageVector = Icons.Sharp.Close,
//                                contentDescription = "Close"
//                            )
//                        }
//                    }
//                },
//                text = {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .wrapContentHeight()
//                    ) {
//
//                        if(offerDetails != null) {
//                            MapAreaContentView(
//                                modifier = Modifier,
//                                offerDetails = offerDetails!!,
//                                eventId = eventId
//                            ) {
//                                offerDetails = null
//                            }
//                        }
//                    }
//                },
//                confirmButton = { }
//            )
//        }





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