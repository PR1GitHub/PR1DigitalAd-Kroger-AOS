package com.purered.pr1digitaladclassic

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.gson.Gson
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import coil.imageLoader
import kotlin.math.floor

import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun AdPageView(
    modifier: Modifier = Modifier,
    adPage: AdPage? = null,
    adId:String,
    location:String,
    onHotSpotClick: ( payload:SpotClickPayload) -> Unit,
    key: Int,
    saveLogEnabled: Boolean
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

    val weeklyAdViewModel: DigitalAdViewModel = viewModel()
    //val logEnabled = weeklyAdViewModel.logEnabled

    LaunchedEffect(fileUrl) {


        val adPageId = adPage?.eventPageId;


        if (adPageId != null) {
            Logger.i("[LOG]  Entered adPageId($adPageId) != null condition", saveLogs = null, sendToDB = false)
            try {


                val adPageData: AdPage = weeklyAdService.getPageDetails(adId=adId, pageId = adPageId, location = location)

                Logger.i("[API-LOG]  {adPageId: $adPageId} getPageDetails Api triggered...", saveLogs = null, sendToDB = false)

                if (adPageData.contents.isNotEmpty()) {

                    Logger.i("[LOG]  {adPageId: $adPageId} Entered adPageData.contents.isNotEmpty() condition.", saveLogs = null, sendToDB = false)

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

                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId,
                        loc = location,
                        offerId = "",
                        url = "",
                        appDetails = "AOS:[API-LOG] [AdPageView.kt]  getPageDetails Api SUCCESS {adPageId: $adPageId}"
                    ))
                    Logger.i("[API-LOG] [AdPageView.kt]  getPageDetails Api SUCCESS {adPageId: $adPageId}", saveLogs = logData, sendToDB = saveLogEnabled)

                }else{
                    println(" Noooo Hot Maps")

                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId,
                        loc = location,
                        offerId = "",
                        url = "",
                        appDetails = "AOS:[API-LOG] [AdPageView.kt]  {adPageId: $adPageId} getPageDetails Api success but entered adPageData.contents.isEmpty() condition. NO Hot Maps"
                    ))
                    Logger.i("[API-LOG] [AdPageView.kt]  {adPageId: $adPageId} getPageDetails Api success but entered adPageData.contents.isEmpty() condition. NO Hot Maps", saveLogs = logData, sendToDB = saveLogEnabled
)

                }

            } catch (e: Exception) {
                println("PAGE DETAILES Loading Failed")
                println(e)

                val logData = SaveLogs(SaveLogDetails(
                    adId = adId,
                    loc = location,
                    offerId = "",
                    url = "",
                    appDetails = "AOS:[API-LOG]  getPageDetails Api FAILED. {adPageId: $adPageId}"
                ))
                Logger.e("[API-LOG]  getPageDetails Api FAILED. {adPageId: $adPageId}", saveLogs = logData, sendToDB = saveLogEnabled
)

            }


        }else{
            val logData = SaveLogs(SaveLogDetails(
                adId = adId,
                loc = location,
                offerId = "",
                url = "",
                appDetails = "AOS:[LOG]  Entered adPageId = null condition."
            ))
            Logger.e("[LOG]  Entered adPageId = null condition.", saveLogs = logData, sendToDB = saveLogEnabled
)
        }


    }



    val onHotMapClickHandler : suspend (selectedMapArea:MapArea) -> Unit = { selectedMapArea ->

        val logData = SaveLogs(SaveLogDetails(
            adId = adId,
            loc = location,
            offerId = "",
            url = "",
            appDetails = "AOS:[HOTMAP-LOG]  Tapped on Hot Map. => selectedMapArea : $selectedMapArea"
        ))
        Logger.i("[HOTMAP-LOG]  Tapped on Hot Map. => selectedMapArea : $selectedMapArea", saveLogs = logData, sendToDB = saveLogEnabled)

        try {



            if (selectedMapArea.contentType?.lowercase() == "creative") {

                val logData1 = SaveLogs(SaveLogDetails(
                    adId = adId,
                    loc = location,
                    offerId = "",
                    url = "",
                    appDetails = "AOS:[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Promo]  selectedMapArea = $selectedMapArea"
                ))
                Logger.i("[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Promo]  selectedMapArea = $selectedMapArea", saveLogs = logData1, sendToDB = saveLogEnabled)

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

                Logger.i("[PAYLOAD] {Promo} $payload", saveLogs = null, sendToDB = false)
                Logger.i("[PAYLOAD]  Promo details payload dispatched.", saveLogs = null, sendToDB = false)
                onHotSpotClick(payload)

                val logData2 = SaveLogs(SaveLogDetails(
                    adId = adId,
                    loc = location,
                    offerId = "",
                    url = "",
                    appDetails = "AOS:[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Promo]  Payload dispatched : $payload"
                ))
                Logger.i("[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Promo]  Calling savelogs Api...", saveLogs = logData2, sendToDB = saveLogEnabled)

            }else{

                val logData1 = SaveLogs(SaveLogDetails(
                    adId = adId,
                    loc = location,
                    offerId = "",
                    url = "",
                    appDetails = "AOS:[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Offer]  selectedMapArea = $selectedMapArea"
                ))
                Logger.i("[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Offer]  selectedMapArea = $selectedMapArea", saveLogs = logData1, sendToDB = saveLogEnabled)

                isOfferLoading = true
                if (selectedMapArea.content?.offerVersionProductGroupId != null) {
                    Logger.i("[HOTMAP-LOG]  Entered offerVersionProductGroupId != null condition", saveLogs = null, sendToDB = false)

                    val offerDetailsList = weeklyAdService.getOfferDetails(
                        adId,
                        selectedMapArea!!.content!!.offerVersionProductGroupId,
                        location = location
                    );

                    Logger.i("[API-LOG]  {offerVersionProductGroupId = ${selectedMapArea!!.content!!.offerVersionProductGroupId} getOfferDetails Api triggered...", saveLogs = null, sendToDB = false)

                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId,
                        loc = location,
                        offerId = "",
                        url = "",
                        appDetails = "AOS:[API-LOG] [AdPageView.kt :  onHotMapClickHandler > Offer]  getOfferDetails Api triggered... {offerVersionProductGroupId = ${selectedMapArea!!.content!!.offerVersionProductGroupId}"
                    ))
                    Logger.i("[API-LOG] [AdPageView.kt :  onHotMapClickHandler > Offer] getOfferDetails Api triggered... {offerVersionProductGroupId = ${selectedMapArea!!.content!!.offerVersionProductGroupId}", saveLogs = logData, sendToDB = saveLogEnabled)

                    if (offerDetailsList != null) {

                        Logger.i("[HOTMAP-LOG]  Entered OfferDetailsList != null condition", saveLogs = null, sendToDB = false)

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

                        Logger.i("[PAYLOAD] {Offer} $payload", saveLogs = null, sendToDB = false)
                        Logger.i("[PAYLOAD]  Offer payload dispatched.", saveLogs = null, sendToDB = false)
                        onHotSpotClick(payload)

                        val logData = SaveLogs(SaveLogDetails(
                            adId = adId,
                            loc = location,
                            offerId = "",
                            url = "",
                            appDetails = "AOS:[HOTMAP-LOG] {Offer} Payload dispatched : $payload"
                        ))
                        Logger.i("[HOTMAP-LOG] {Offer} Payload dispatched : $payload", saveLogs = logData, sendToDB = saveLogEnabled)

                    }
                }
                else {
                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId,
                        loc = location,
                        offerId = "",
                        url = "",
                        appDetails = "AOS:[LOG]  offerVersionProductGroupId == null for selectedMapArea : $selectedMapArea ;; [ getOfferDetails Api will not be triggered]."
                    ))
                    Logger.e("[LOG]  offerVersionProductGroupId == null for selectedMapArea : $selectedMapArea ;; [ getOfferDetails Api will not be triggered].", saveLogs = logData, sendToDB = saveLogEnabled)
                }
            }


        }catch (e: Exception) {
            println("Error fetching offer details ${e.message}")

            Logger.e("[LOG]  {onHotMapClickHandler > catch block} Error fetching offerDetails ${e.message}", saveLogs = null, sendToDB = false)

            val logData = SaveLogs(SaveLogDetails(
                adId = "",
                loc = "",
                offerId = "",
                url = "",
                appDetails = "AOS:[LOG]  {onHotMapClickHandler > catch block} Error : ${e.message} ; selectedMapArea = $selectedMapArea"
            ))
            Logger.e("[LOG]  {onHotMapClickHandler > catch block} Error : ${e.message} ; selectedMapArea = $selectedMapArea", saveLogs = logData, sendToDB = saveLogEnabled)

        }
        finally {
            isOfferLoading = false
        }


    }

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
                    Logger.i("[DRAW-HOTMAP-LOG] {onMapAreaClick}", saveLogs = null, sendToDB = false)

                    val logData = SaveLogs(SaveLogDetails(
                        adId = "",
                        loc = "",
                        offerId = "",
                        url = "",
                        appDetails = "AOS:[DRAW-HOTMAP-LOG]  {onMapAreaClick} Entered DrawHotMaps."
                    ))
                    Logger.i("[DRAW-HOTMAP-LOG]  {onMapAreaClick} Entered DrawHotMaps.", saveLogs = logData, sendToDB = saveLogEnabled)

                    coroutineScope.launch {

                        onHotMapClickHandler.invoke(it)

                        val logData = SaveLogs(SaveLogDetails(
                            adId = "",
                            loc = "",
                            offerId = "",
                            url = "",
                            appDetails = "AOS:[DRAW-HOTMAP-LOG]  {onMapAreaClick} HANDLER INVOKED."
                        ))
                        Logger.i("[DRAW-HOTMAP-LOG]  {onMapAreaClick} HANDLER INVOKED.", saveLogs = logData, sendToDB = saveLogEnabled)

                    }

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
//            .border(2.dp, Color.Black)
            .background(Color.Transparent)
    ) {
        hotMaps.forEach { mapArea ->
            val left = floor(mapArea.x1).dp
            val top = floor(mapArea.y1).dp
            val right = floor(mapArea.x2).dp
            val bottom = floor(mapArea.y2).dp
//Color.LightGray.copy(alpha = 0.4f)
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .offset(x = left, y = top)
                    .width(right - left)
                    .height(bottom - top)
                    .background(Color.Transparent)
                    //.border(1.dp, Color.Transparent)
                    .border(1.dp, Color.Red)
//                    .clickable {
//                        onMapAreaClick(mapArea)
//                    }

//                    /* -- Trail - 0 --
//                    .pointerInput(Unit) {
//                        awaitPointerEventScope {
//                            while (true) {
//                                val event = awaitPointerEvent()
//                                if (event.changes.any { it.changedToDown() }) {
//                                    onMapAreaClick(mapArea)
//                                }
//                            }
//                        }
//                     }
//                    */

                    /* -- Using double-tap for hotmap --
                    .pointerInput(Unit) {
                        var lastTapTime = 0L
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val currentTime = System.currentTimeMillis()

                                // Detecting a multi-touch event
                                if (event.changes.count { it.changedToDown() } >= 2) {
                                    // Check if this tap is within a short time frame (e.g., 300 ms)
                                    if (currentTime - lastTapTime < 300) {
                                        onMapAreaClick(mapArea)  // Trigger on double-tap
                                    }
                                    lastTapTime = currentTime
                                }
                            }
                        }
                    }
                    */

//                    /* -- Gestures with Delay -- Trail - 2
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()

                                // Detect initial touch
                                val downCount = event.changes.count { it.changedToDown() }

                                if (downCount == 1) {
                                    // Record the initial time
                                    val startTime = System.nanoTime()

                                    // Wait and check for additional touches
                                    var stillSingleTouch = true
                                    while (System.nanoTime() - startTime < 200_000_000L) { // 200ms in nanoseconds
                                        val nextEvent = awaitPointerEvent()
                                        val activeTouches = nextEvent.changes.count { it.pressed }

                                        if (activeTouches > 1) {
                                            stillSingleTouch = false
                                            break
                                        }
                                    }

                                    // If no additional touches were detected, trigger the method
                                    if (stillSingleTouch) {
                                        onMapAreaClick(mapArea)
                                    }

                                    // Consume the pointer event to prevent lingering state
                                    event.changes.forEach { it.consume() }
                                } else if (downCount > 1) {
                                    // Consume multi-touch events immediately
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        }
                    }

                    /* -- Gestures with Delay -- Trail - 1
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                Log.i("GESTURES","Entered while loop")
                                // Detect initial touch
                                val downCount = event.changes.count { it.changedToDown() }
                                Log.i("GESTURES","downCount = $downCount")
                                if (downCount == 1) {
                                    // Record the initial time
                                    val startTime = System.currentTimeMillis()
                                    Log.i("GESTURES","startTime = $startTime")
                                    // Wait and check for additional touches in a loop
                                    var stillSingleTouch = true
                                    Log.i("GESTURES","stillSingleTouch = $stillSingleTouch")
                                    while (System.currentTimeMillis() - startTime < 200) { // 200ms delay
                                        Log.i("GESTURES","Time difference = ${System.currentTimeMillis() - startTime}")
                                        Log.i("GESTURES","Entered time delay while loop")
                                        val nextEvent = awaitPointerEvent()
                                        val activeTouches = nextEvent.changes.count { it.pressed }
                                        Log.i("GESTURES","activeTouches = $activeTouches")
                                        if (activeTouches > 1) {
                                            Log.i("GESTURES","Entered activeTouches > 1 condition")
                                            stillSingleTouch = false
                                            Log.i("GESTURES","stillSingleTouch = $stillSingleTouch")
                                            break
                                        }
                                    }

                                    // If no additional touches were detected, trigger the method
                                    if (stillSingleTouch) {
                                        Log.i("GESTURES","No additional touches detected")
                                        Log.i("GESTURES","Triggering onMapAreaClick(mapArea) method")
                                        onMapAreaClick(mapArea)
                                    }

                                    // Consume the pointer event to prevent lingering state
                                    event.changes.forEach { it.consume() }
                                } else if (downCount > 1) {
                                    // Consume multi-touch events immediately
                                    Log.i("GESTURES","Entered downCount > 1 condition")
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        }
                    }
//                    */


            ) {
                // Optionally, you can add content to each Box here, like labels or icons
//                Text(
//                    text = "Hot Map",
//                    color = Color.Red,
//                    fontSize = 14.sp,
//                    modifier = Modifier.align(Alignment.Center)
//                )
            }
        }
    }
}