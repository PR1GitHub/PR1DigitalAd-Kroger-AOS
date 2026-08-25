package com.purered.pr1digitaladclassic

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

const val DigitalAdLibVersion = "0.0.12" // This is the version of the DigitalAd library

data class SpotClickPayload(
    val itemType: String,
    val id: String,
    val headline: String,
    val bodyCopy: String,
    val imageURL: String,
    val pricingHTML: String,
    val pricingText: String,
    val upc: String,
    val startDate: String,
    val endDate: String,
    val category: String,
    val disclaimer: String,
    val webURL: String,
    val appURL: String,
    val promoAltText:String? = "",
    val promoEventName:String? = "",
    val isCoupon: Boolean? = false,
    val isShoppable: Boolean? = false,
)

data class ZoomButtonsConfig (
    val enable: Boolean = true,
    val offsetY: Int = -140,
)

@Composable
fun DigitalAd(
    modifier: Modifier = Modifier,
    adId: String,
    location: String,
    apiKey: String,
    apiEnv: ApiEnv,
    adExperience: AdExperience = AdExperience.oneAd,
    //zoomControls: Boolean = true,
    //zoomControlsOffset: Int = -140,
    zoomButtonsConfig: ZoomButtonsConfig = ZoomButtonsConfig(),
    onHotSpotClick: (payload:SpotClickPayload) -> Unit,
    onAdLoaded: (totalPages: Int) -> Unit = {},
    onAdPageChanged: (totalPages: Int, currentPageIndex: Int, adPageId: String) -> Unit = { _, _, _ -> }
) {

    //val isHorizontalView = true
    weeklyAdService = createWeeklyAdService(apiEnv,apiKey)

    //val mode = "public"
    val weeklyAdViewModel: DigitalAdViewModel = viewModel()
    val viewState by weeklyAdViewModel.digitalAdState

    // Enable or disable local logging
    Logger.isLoggingEnabled = true // Set to `false` to disable local logs globally

    LaunchedEffect(Unit) {
        weeklyAdViewModel.fetchAdDetails(adId,location)
    }
    Box(modifier = modifier) {
        when {
            viewState.loading -> {
                Logger.i("[API-LOG]  Loading...", saveLogs = null, sendToDB = false)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 440.dp)
                        .padding(16.dp)
                        .shimmerEffect()
                )
            }
            viewState.error != null -> {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = "Something went wrong. Please try again.")
                    TextButton(onClick = { weeklyAdViewModel.reloadWeeklyAd() }) {
                        Text("Try Again")
                    }
                }

                val logData = SaveLogs(SaveLogDetails(
                    adId = adId, loc = location,
                    appDetails = "AOS:[LOG] [DigitalAd.kt]  Something went wrong. Please try again. {adId: $adId, location: $location}"
                ))
                Logger.e("${logData.value.appDetails}", saveLogs = logData, sendToDB = false)
            }
            else -> {
                if(viewState.weeklyAd != null) {
                    val ad = viewState.weeklyAd!!

                    LaunchedEffect(ad) {
                        onAdLoaded(ad.pages.count())
                    }

                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId, loc = location,
                        appDetails = "AOS:[LOG] [DigitalAd.kt]  WeeklyAd data fetched. {adId: $adId, location: $location}"
                    ))
                    Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = ad.isLogEnabled)

                    if (adExperience == AdExperience.oneAd) {
                        HorizontalDigitalAdView(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            ad = ad,
                            adId = adId,
                            location = location,
                            zoomButtonsConfig = zoomButtonsConfig,
                            onHotSpotClick = onHotSpotClick,
                            onAdPageChanged = onAdPageChanged
                        )
                    } else {
                        VerticalDigitalAdView(
                            modifier = Modifier.fillMaxSize(),
                            ad = ad,
                            adId = adId,
                            location = location,
                            zoomButtonsConfig = zoomButtonsConfig,
                            onHotSpotClick = onHotSpotClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun HorizontalDigitalAdView(
    modifier: Modifier = Modifier,
    ad: WeeklyAd,
    adId: String,
    location: String,
    zoomButtonsConfig: ZoomButtonsConfig,
    onHotSpotClick: (SpotClickPayload) -> Unit,
    onAdPageChanged: (totalPages: Int, currentPageIndex: Int, adPageId: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val actualPageCount = ad.pages.size

    var aspectRatio by remember { mutableFloatStateOf(0.826f) }

    // Looping behavior: Use a large virtual page count and modulo for actual content
    val loopingFactor = 1000
    val virtualPageCount = if (actualPageCount > 1) actualPageCount * loopingFactor else actualPageCount
    val initialPage = if (actualPageCount > 1) (virtualPageCount / 2) else 0

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { virtualPageCount }
    )

    // State to track if the user has reached the last page for the first time
    var hasReachedLastPage by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        val actualIndex = pagerState.currentPage % actualPageCount
        if (actualIndex == actualPageCount - 1) {
            hasReachedLastPage = true
        }
        onAdPageChanged(actualPageCount, actualIndex,ad.pages[actualIndex].adPageId)
    }

    // Directional swiping: Block backward looping from the first page until the end is reached once
    val directionalScrollConnection = remember(hasReachedLastPage, pagerState, actualPageCount) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isAtFirstPage = (pagerState.currentPage % actualPageCount) == 0
                return if (!hasReachedLastPage && available.x > 0 && source == NestedScrollSource.UserInput && isAtFirstPage) {
                    // Block user drags that would loop backward from first page
                    available
                } else {
                    Offset.Zero
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        ZoomableBoxContent(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            content = {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .nestedScroll(directionalScrollConnection),
                    verticalAlignment = Alignment.Top
                ) { virtualPageIndex ->
                    val actualPageIndex = virtualPageIndex % actualPageCount
                    val adPage = ad.pages[actualPageIndex]
                    if (adPage.fileURL.isNotEmpty()) {
                        AdPageView(
                            adPage = adPage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(aspectRatio),
                            adId = adId,
                            location = location,
                            onHotSpotClick = onHotSpotClick,
                            key = virtualPageIndex,
                            saveLogEnabled = ad.isLogEnabled,
                            isScrollable = false,
                            onSizeCalculated = { size ->
                                if (size.width > 0 && size.height > 0) {
                                    val newRatio = size.width / size.height
                                    if (aspectRatio != newRatio) {
                                        aspectRatio = newRatio
                                    }
                                }
                            }
                        )
                    }
                }

                Log.i("isLogEnabled", "ad.isLogEnabled = ${ad.isLogEnabled}")

                val logData = SaveLogs(
                    SaveLogDetails(
                        adId = adId,
                        loc = location,
                        appDetails = "AOS:[LOG] [DigitalAd.kt] Generating AdPageView... {adId: $adId, location: $location}"
                    )
                )
                Logger.i(
                    "${logData.value.appDetails}",
                    saveLogs = logData,
                    sendToDB = ad.isLogEnabled
                )
            },
            enableZoomButtons = zoomButtonsConfig.enable,
            zoomButtonOffset = zoomButtonsConfig.offsetY,
        )

        PagerIndicators(
            pageCount = actualPageCount,
            currentPage = pagerState.currentPage % actualPageCount,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp, bottom = 16.dp),
            onPageSelected = { index ->
                coroutineScope.launch {
                    val currentVirtualPage = pagerState.currentPage
                    val currentActualPage = currentVirtualPage % actualPageCount
                    val targetVirtualPage = currentVirtualPage + (index - currentActualPage)
                    pagerState.animateScrollToPage(targetVirtualPage)
                }
            }
        )
    }
}

@Composable
internal fun VerticalDigitalAdView(
    modifier: Modifier = Modifier,
    ad: WeeklyAd,
    adId: String,
    location: String,
    zoomButtonsConfig: ZoomButtonsConfig,
    onHotSpotClick: (SpotClickPayload) -> Unit
) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f)) {
            ZoomableBoxContent(
                modifier = Modifier.fillMaxSize(),
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        ad.pages.forEachIndexed { index, adPage ->
                            if (adPage.fileURL.isNotEmpty()) {
                                AdPageView(
                                    adPage = adPage,
                                    modifier = Modifier.fillMaxWidth(),
                                    adId = adId,
                                    location = location,
                                    onHotSpotClick = onHotSpotClick,
                                    key = index,
                                    saveLogEnabled = ad.isLogEnabled,
                                    isScrollable = false
                                )
                            }
                        }
                    }

                    Log.i("isLogEnabled", "ad.isLogEnabled = ${ad.isLogEnabled}")

                    val logData = SaveLogs(
                        SaveLogDetails(
                            adId = adId,
                            loc = location,
                            appDetails = "AOS:[LOG] [DigitalAd.kt] Generating AdPageView... {adId: $adId, location: $location}"
                        )
                    )
                    Logger.i(
                        "${logData.value.appDetails}",
                        saveLogs = logData,
                        sendToDB = ad.isLogEnabled
                    )
                },
                enableZoomButtons = zoomButtonsConfig.enable,
                zoomButtonOffset = zoomButtonsConfig.offsetY,
            )
        }
    }
}

/*-- PAGE INDICATORS --*/
// Only this many dots are ever on screen. Pages are grouped into blocks of this size: the
// active dot walks across the block, and reaching the end swaps the band to the next block.
// Dots on a side that still has pages beyond the block shrink towards the edge.
private const val MaxVisibleIndicatorDots = 10
private val IndicatorDotSize = 8.dp
private val IndicatorDotSpacing = 8.dp

@Composable
internal fun PagerIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    maxVisibleDots: Int = MaxVisibleIndicatorDots,
    dotSize: Dp = IndicatorDotSize,
    dotSpacing: Dp = IndicatorDotSpacing,
    onPageSelected: (Int) -> Unit
) {
    if (pageCount <= 1) return

    val visibleCount = minOf(pageCount, maxVisibleDots.coerceAtLeast(1))
    // The band is the block of pages the current page falls in. The trailing block is
    // pulled back so it stays full width instead of rendering a stub of a few dots.
    val windowStart = (currentPage / visibleCount * visibleCount)
        .coerceIn(0, pageCount - visibleCount)
    val windowEnd = windowStart + visibleCount - 1
    val hasMoreBefore = windowStart > 0
    val hasMoreAfter = windowEnd < pageCount - 1

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in windowStart..windowEnd) {
            // How close this dot is to an edge that still has pages beyond it.
            val edgeDistance = minOf(
                if (hasMoreBefore) index - windowStart else Int.MAX_VALUE,
                if (hasMoreAfter) windowEnd - index else Int.MAX_VALUE
            )
            val targetScale = when {
                // The active dot is always full size, even when it sits on a shrunk slot.
                index == currentPage -> 1f
                edgeDistance == 0 -> 0.5f
                edgeDistance == 1 -> 0.75f
                else -> 1f
            }
            val scale by animateFloatAsState(
                targetValue = targetScale,
                label = "page indicator scale"
            )
            val color = if (currentPage == index) Color.DarkGray else Color.LightGray
            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    // Keeps the tap target (and the pitch between dots) constant while
                    // the dot itself scales.
                    .size(dotSize + dotSpacing)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Go to page ${index + 1} of $pageCount"
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onPageSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}
