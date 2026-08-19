package com.purered.pr1digitaladclassic

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.absoluteValue
import kotlin.math.floor


@Composable
internal fun AdPageView(
    modifier: Modifier = Modifier,
    adPage: AdPage? = null,
    adId: String,
    location: String,
    onHotSpotClick: (payload: SpotClickPayload) -> Unit,
    key: Int,
    saveLogEnabled: Boolean,
    isScrollable: Boolean = true,
    onSizeCalculated: ((Size) -> Unit)? = null,
) {
    var imageWidth by remember { mutableStateOf(0f) }
    var imageHeight by remember { mutableStateOf(0f) }
    var imageState by remember { mutableStateOf("loading") }
    var displaySize by remember { mutableStateOf(Size.Zero) }
    var dpiImageWidth by remember { mutableStateOf(0.dp) }
    var dpiImageHeight by remember { mutableStateOf(0.dp) }
    val coroutineScope = rememberCoroutineScope()
    var pageHotMaps by remember { mutableStateOf(emptyList<MapArea>()) }
    var convertedHotMaps by remember { mutableStateOf(emptyList<MapArea>()) }
    var isOfferLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fileUrl = adPage?.compressedFileURL

    dpiImageWidth = pixelsToDp(displaySize.width)
    dpiImageHeight = pixelsToDp(displaySize.height)

    val wDp = pixelsToDp(displaySize.width)
    val hDp = pixelsToDp(displaySize.height)

    val scrollState = rememberScrollState()

    LaunchedEffect(fileUrl) {
        val adPageId = adPage?.eventPageId
        if (adPageId != null) {
            Logger.i("[LOG]  Entered adPageId($adPageId) != null condition", saveLogs = null, sendToDB = false)
            try {
                val adPageData: AdPage = weeklyAdService.getPageDetails(adId = adId, pageId = adPageId, location = location)

                val logData = SaveLogs(SaveLogDetails(
                    adId = adId, loc = location,
                    appDetails = "AOS:[API-LOG-GetPageDetails]  {adPageId: $adPageId} getPageDetails Api triggered... { apiRequest : https://oms-kroger-webapp-da-classic-api-prod.przone.net/api/dacs/$adId/pages/$adPageId?location=$location }"
                ))
                Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)

                if (adPageData.contents.isNotEmpty()) {
                    val logData1 = SaveLogs(SaveLogDetails(
                        adId = adId, loc = location,
                        appDetails = "AOS:[LOG-GetPageDetails]  {adPageId: $adPageId} Entered adPageData.contents.isNotEmpty() condition."
                    ))
                    Logger.i("${logData1.value.appDetails}", saveLogs = logData1, sendToDB = saveLogEnabled)

                    val request = ImageRequest.Builder(context)
                        .data(fileUrl)
                        .size(coil.size.Size.ORIGINAL)
                        .build()
                    val result = (context.imageLoader.execute(request) as SuccessResult).drawable
                    imageWidth = result.intrinsicWidth.toFloat()
                    imageHeight = result.intrinsicHeight.toFloat()

                    var localPageHotMaps: List<MapArea> = emptyList()
                    adPageData.contents.forEachIndexed { index, pageContent ->
                        try {
                            val mapArea = convertJsonToMapArea(pageContent.mapConfig)
                            localPageHotMaps = localPageHotMaps + mapArea
                        } catch (_: Exception) {
                            Logger.i("AOS:[PAGE-HOTMAPS-GSON-GetPageDetails]  Conversion for mapArea at index = $index FAILED", saveLogs = null, sendToDB = false)
                        }
                    }
                    pageHotMaps = localPageHotMaps
                } else {
                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId, loc = location,
                        appDetails = "AOS:[API-LOG-GetPageDetails] [AdPageView.kt]  getPageDetails Api success but entered adPageData.contents.isEmpty() condition. NO Hot Maps"
                    ))
                    Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)
                }
            } catch (_: Exception) {
                val logData = SaveLogs(SaveLogDetails(
                    adId = adId, loc = location,
                    appDetails = "AOS:[API-LOG-GetPageDetails]  getPageDetails Api FAILED. {adId = $adId, eventPageId = $adPageId, location = $location}"
                ))
                Logger.e("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)
            }
        }
    }

    LaunchedEffect(pageHotMaps, imageWidth, imageHeight, dpiImageWidth, dpiImageHeight) {
        if (pageHotMaps.isNotEmpty() && imageWidth > 0f && imageHeight > 0f && dpiImageWidth > 0.dp && dpiImageHeight > 0.dp) {
            convertedHotMaps = convertHotMapsToDisplaySize(
                hotMaps = pageHotMaps,
                originalImageWidth = imageWidth,
                originalImageHeight = imageHeight,
                displayWidth = dpiImageWidth.value,
                displayHeight = dpiImageHeight.value
            )
            val logData = SaveLogs(SaveLogDetails(
                adId = adId, loc = location,
                appDetails = "AOS:[PAGE-HOTMAPS-SCALE_DOWN-GetPageDetails] [AdPageView.kt] convertedHotMaps updated"
            ))
            Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)
        }
    }



    val onHotMapClickHandler : suspend (selectedMapArea:MapArea) -> Unit = { selectedMapArea ->

        val logData = SaveLogs(SaveLogDetails(
            adId = adId, loc = location,
            appDetails = "AOS:[HOTMAP-LOG]  Tapped on Hot Map. => selectedMapArea : $selectedMapArea"
        ))
        Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)

        try {

            if (selectedMapArea.contentType?.lowercase() == "creative") {

                val logData1 = SaveLogs(SaveLogDetails(
                    adId = adId, loc = location,
                    appDetails = "AOS:[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Promo]  selectedMapArea = $selectedMapArea"
                ))
                Logger.i("${logData1.value.appDetails}", saveLogs = logData1, sendToDB = saveLogEnabled)

                var idVal = "0"

                try {
                    if(selectedMapArea.contentId != null) {
                        idVal = selectedMapArea.contentId

                        val logData = SaveLogs(SaveLogDetails(
                            appDetails = "AOS:[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Promo]  selectedMapArea.contentId(${selectedMapArea.contentId}) != null ;; id_to_string = $idVal"
                        ))
                        Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)
                    }
                } catch (_: NumberFormatException) {
                    println("Error: The string is not a valid integer")

                    val logData = SaveLogs(SaveLogDetails(
                        appDetails = "AOS:[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Promo]  selectedMapArea.contentId(${selectedMapArea.contentId}) is not a valid integer"
                    ))
                    Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)
                }

                var webURLVal=""
                var appURLVal=""
                var patVal = ""
                var peVal=""

                if( selectedMapArea.content != null){
                    webURLVal= selectedMapArea.content!!.webUrl
                    appURLVal= selectedMapArea.content!!.appUrl
                    patVal = selectedMapArea.content!!.altText
                    peVal = selectedMapArea.content!!.event

                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId, loc = location,
                        appDetails = "AOS:[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Promo]  Entered selectedMapArea.content != null condition"
                    ))
                    Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)
                }
                else {
                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId, loc = location,
                        appDetails = "AOS:[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Promo]  Entered selectedMapArea.content == null condition"
                    ))
                    Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)
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
                    promoEventName = peVal,
                    isCoupon = false,
                    isShoppable = false
                )

                Logger.i("[PAYLOAD] {Promo} $payload", saveLogs = null, sendToDB = false)
                Logger.i("[PAYLOAD]  Promo details payload dispatched.", saveLogs = null, sendToDB = false)
                onHotSpotClick(payload)

                val logData2 = SaveLogs(SaveLogDetails(adId = adId, loc = location, appDetails = "AOS:[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Promo]  Payload dispatched : $payload"))
                Logger.i("${logData2.value.appDetails}", saveLogs = logData2, sendToDB = saveLogEnabled)

            }
            else{

                val logData = SaveLogs(SaveLogDetails(
                    adId = adId, loc = location,
                    appDetails = "AOS:[HOTMAP-LOG] [AdPageView.kt : onHotMapClickHandler > Offer]  selectedMapArea = $selectedMapArea"
                ))
                Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)

                isOfferLoading = true

                if (selectedMapArea.content?.offerVersionProductGroupId != null) {
                    Logger.i("[HOTMAP-LOG]  Entered offerVersionProductGroupId != null condition", saveLogs = null, sendToDB = false)

                    val offerDetailsList = weeklyAdService.getOfferDetails(
                        adId,
                        selectedMapArea.content!!.offerVersionProductGroupId,
                        location = location
                    )

                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId, loc = location,
                        appDetails = "AOS:[API-LOG] [AdPageView.kt :  onHotMapClickHandler > Offer]  getOfferDetails Api triggered... { offerVersionProductGroupId = ${selectedMapArea.content!!.offerVersionProductGroupId}, apiRequest = https://oms-kroger-webapp-da-classic-api-prod.przone.net/api/dacs/$adId/offers/${selectedMapArea.content!!.offerVersionProductGroupId} }"
                    ))
                    Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)

                    val logData1 = SaveLogs(SaveLogDetails(
                        adId = adId, loc = location,
                        appDetails = "AOS:[API-LOG]  getOfferDetails api response : $offerDetailsList"
                    ))
                    Logger.i("${logData1.value.appDetails}", saveLogs = logData1, sendToDB = false)

                    if (offerDetailsList != null) {

                        Logger.i("[HOTMAP-LOG]  Entered OfferDetailsList != null condition", saveLogs = null, sendToDB = false)

                        val resOfferDetails:OfferDetails = offerDetailsList

                        val appUrlString = resOfferDetails.appURL ?: resOfferDetails.appUrl ?: ""

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
                            //appURL = resOfferDetails.appURL ?: "",
                            appURL = appUrlString,
                            isCoupon = resOfferDetails.isCoupon,
                            isShoppable = resOfferDetails.isShoppable
                        )

                        Logger.i("[PAYLOAD] {Offer} $payload", saveLogs = null, sendToDB = false)
                        Logger.i("[PAYLOAD]  Offer payload dispatched.", saveLogs = null, sendToDB = false)
                        onHotSpotClick(payload)

                        val logData = SaveLogs(SaveLogDetails(adId = adId, loc = location, appDetails = "AOS:[HOTMAP-LOG] {Offer} Payload dispatched : $payload"))
                        Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)

                    }
                }
                else {
                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId, loc = location,
                        appDetails = "AOS:[LOG]  offerVersionProductGroupId == null for selectedMapArea : $selectedMapArea ;; [ getOfferDetails Api will not be triggered]."
                    ))
                    Logger.e("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)
                }
            }


        } catch (e: Exception) {
            Logger.e("[LOG]  {onHotMapClickHandler > catch block} Error fetching offerDetails ${e.message}", saveLogs = null, sendToDB = false)

            val logData = SaveLogs(SaveLogDetails(
                adId = "", loc = "",
                appDetails = "AOS:[LOG]  {onHotMapClickHandler > catch block} Error : ${e.message} ; selectedMapArea = $selectedMapArea"
            ))
            Logger.e("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)
        }
        finally {
            isOfferLoading = false
        }

    }

    Box(
        modifier = modifier
            .border(1.dp, Color.White)
            .clipToBounds()
            .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)
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

        AsyncImage(model = fileUrl, contentDescription = null, contentScale = ContentScale.FillWidth, onLoading = {
            imageState = "loading"
        }, modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { layoutCoordinates ->
                val size = layoutCoordinates.size.toSize()
                displaySize = size
                if (size.width > 0 && size.height > 0) {
                    onSizeCalculated?.invoke(size)
                }
            }, onError = {
            imageState = "error"
        }, onSuccess = { _ ->
            imageState = "success"
        })
        if (imageState == "success" && convertedHotMaps.isNotEmpty()) {

            //  println(" DrawHotMaps DrawHotMaps DrawHotMaps DrawHotMaps")

            val logData = SaveLogs(SaveLogDetails(appDetails = "AOS:[DRAW-HOTMAP-LOG]  Sending convertedHotMaps to DrawHotMaps() for pageKey($key) = $convertedHotMaps"))
            Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)

            DrawHotMaps(
                hotMaps = convertedHotMaps,
                width = displaySize.width,
                height = displaySize.height,
                pageKey = key,
                onMapAreaClick = {
                    Logger.i("[DRAW-HOTMAP-LOG] {onMapAreaClick}", saveLogs = null, sendToDB = false)

                    val logData = SaveLogs(SaveLogDetails(appDetails = "AOS:[DRAW-HOTMAP-LOG]  {onMapAreaClick} Entered DrawHotMaps."))
                    Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)

                    coroutineScope.launch {

                        onHotMapClickHandler.invoke(it)

                        val logData = SaveLogs(SaveLogDetails(appDetails = "AOS:[DRAW-HOTMAP-LOG]  {onMapAreaClick} HANDLER INVOKED."))
                        Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = saveLogEnabled)

                    }

                    /*
                    if (it.contentType?.lowercase() == "creative") {
                        Toast.makeText(
                            context,
                            "Clicked on Creative spot will be redirected to ${it.content?.webUrl}",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        isOfferLoading = true
                        coroutineScope.launch {
                            loadOfferDetails.invoke(it)
                        }
                        //isPopupVisible = true
                    }
                    */

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

    val hotMapViewModel:HotMapViewModel = viewModel()
    val logData = SaveLogs(SaveLogDetails(appDetails = "AOS:[DRAW-HOTMAP-LOG]  HotMaps receievd by DrawHotMaps() for pageKey($pageKey) = $hotMaps"))
    Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = false)

    if (hotMaps.isEmpty()) {
        return
    }


    val context = LocalContext.current
    val wDp = pixelsToDp(width)
    val hDp = pixelsToDp(height)
    val coroutineScope = rememberCoroutineScope()
    var lastTapTime by remember { mutableStateOf(0L) }
    val tapCooldown = 500L

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
            // Color.LightGray.copy(alpha = 0.4f)
            Box(
                modifier = Modifier
                    .offset(x = left, y = top)
                    .width(right - left)
                    .height(bottom - top)
                    .background(Color.Transparent)
                    .border(1.dp, Color.Transparent)
                    .semantics(mergeDescendants = true) {
                        role = Role.Button
                        contentDescription = buildHotspotContentDescription(mapArea)
                        onClick(label = "Open offer details") {
                            onMapAreaClick(mapArea)
                            true
                        }
                    }
                    .pointerInput(Unit) {
                        forEachGesture {
                            awaitPointerEventScope {
                                val down = awaitFirstDown(requireUnconsumed = false) // Detect initial touch
                                val touchSlop = viewConfiguration.touchSlop // System-defined threshold
                                val velocityTracker = VelocityTracker() // Track movement speed
                                var isScrolling = false
                                var isFling = false

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: continue

                                    velocityTracker.addPosition(change.uptimeMillis, change.position) // Track movement

                                    if (change.positionChange().getDistance() > touchSlop) {
                                        isScrolling = true // Mark as scrolling if movement exceeds threshold
                                    }

                                    if (change.pressed.not()) { // Finger lifted
                                        val velocity = velocityTracker.calculateVelocity()
                                        isFling = velocity.x.absoluteValue > 1000 || velocity.y.absoluteValue > 1000
                                        break
                                    }
                                }

                                if (isFling) {
                                    Log.d("DrawHotMaps", "Fling detected! Ignoring tap")
                                    return@awaitPointerEventScope
                                }

                                if (isScrolling) {
                                    Log.d("DrawHotMaps", "Scrolling detected, ignoring tap")
                                    return@awaitPointerEventScope
                                }

                                if (hotMapViewModel.isZooming) { // ✅ Check if zooming before triggering tap
                                    Log.d("DrawHotMaps", "Zoom detected, ignoring tap")
                                    return@awaitPointerEventScope
                                }

                                // **Valid tap detected**
                                Log.d("DrawHotMaps", "Tap detected on HotMap, triggering click")
                                onMapAreaClick(mapArea)
                            }
                        }
                    }

            )
        }
    }
}

private fun buildAdPageContentDescription(adPage: AdPage?): String {
    val pageNumber = adPage?.page?.takeIf { it.isNotBlank() }
    return if (pageNumber != null) {
        "Weekly ad page $pageNumber"
    } else {
        "Weekly ad page"
    }
}

private fun buildHotspotContentDescription(mapArea: MapArea): String {
    val content = mapArea.content
    //Log.i("buildHotspotContentDescription", "mapArea: $mapArea, content: $content")

    val contentType = mapArea.contentType?.lowercase()
    var talkbackString = ""

    if (contentType == "creative") {
        val altText = content?.altText?.takeIf { it.isNotBlank() }
        // ToDo : Need to work on object parsing to avoid string null check
        talkbackString = if (altText != null && altText != "null") {
            "Promotional offer, $altText"
        } else {
            "Promotional offer image"
        }
    } else {
        val headline = content?.headline?.takeIf { it.isNotBlank() }
        talkbackString = if (headline != null) {
            "Offer on, $headline"
        } else {
            "Offer"
        }
    }

    return talkbackString
}

internal fun convertJsonToMapArea(jsonString: String): MapArea {
    val jsonObject = JSONObject(jsonString)

    val contentObject = jsonObject.optJSONObject("content")

    val mapAreaContent = contentObject?.let {
        MapAreaContent(
            altText = it.optString("altText", ""),
            appUrl = it.optString("appUrl", ""),
            event = it.optString("event", ""),
            id = it.optString("id", ""),
            webUrl = it.optString("webUrl", ""),
            bodyCopy = it.optString("bodyCopy", ""),
            headline = it.optString("headline", ""),
            imageURL = it.optString("imageURL", ""),
            index = it.optString("index", ""),
            offerVersionProductGroupId = it.optString("offerVersionProductGroupId", ""),
        )
    }

    return MapArea(
        x1 = jsonObject.optDouble("x1", 0.0).toFloat(),
        y1 = jsonObject.optDouble("y1", 0.0).toFloat(),
        x2 = jsonObject.optDouble("x2", 0.0).toFloat(),
        y2 = jsonObject.optDouble("y2", 0.0).toFloat(),
        lineWidth = jsonObject.optInt("lineWidth", 1),
        color = jsonObject.optString("color", "DeepSkyBlue"),
        isSelected = jsonObject.optBoolean("isSelected", false),
        hasAttached = jsonObject.optBoolean("hasAttached", false),
        contentId = jsonObject.optString("contentId", ""),
        contentType = jsonObject.optString("contentType", ""),
        eventPageContentId = jsonObject.optString("eventPageContentId", ""),
        content = mapAreaContent
    )
}
