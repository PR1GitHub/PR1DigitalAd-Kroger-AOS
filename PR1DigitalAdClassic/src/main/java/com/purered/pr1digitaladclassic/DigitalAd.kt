package com.purered.pr1digitaladclassic

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

// Set from the Gradle publication version, so it always matches the released artifact.
val DigitalAdLibVersion: String = BuildConfig.LIB_VERSION

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

/*-- ERROR REPORTING --*/
enum class AdErrorType {
    /** getAdDetails failed - nothing renders. The SDK shows its retry UI. */
    adLoadFailed,
    /** Ad loaded but contains no pages - nothing renders. */
    adEmpty,
    /** getPageDetails or the page's image dimensions failed - the page image may still
     *  show, but its hotspots will not work. */
    pageDetailsFailed,
    /** A page image failed to render. */
    pageImageFailed,
    /** A hotspot was tapped but the offer could not be loaded - no payload dispatched. */
    offerLoadFailed,
}

data class AdErrorPayload(
    val type: AdErrorType,
    val message: String,
    /** Set for page-scoped errors (pageDetailsFailed, pageImageFailed, offerLoadFailed). */
    val adPageId: String? = null,
    /** True when the SDK offers its own recovery (e.g. the ad-load retry button). */
    val isRecoverable: Boolean = false,
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
    onAdPageChanged: (totalPages: Int, currentPageIndex: Int, adPageId: String) -> Unit = { _, _, _ -> },
    onAdError: (payload: AdErrorPayload) -> Unit = {}
) {

    // One service per (env, key), created once per configuration instead of a global
    // reassigned on every recomposition - two DigitalAd instances with different
    // configs no longer clobber each other. The .also hands Logger its telemetry
    // handle before any sendToDB logging happens in this composition.
    val adService = remember(apiEnv, apiKey) {
        createWeeklyAdService(apiEnv, apiKey).also { telemetryAdService = it }
    }

    // Keyed so two DigitalAd instances on one screen do not share load state; two
    // instances showing the SAME ad intentionally share one ViewModel and one fetch.
    val weeklyAdViewModel: DigitalAdViewModel = viewModel(key = "PR1DigitalAd:$adId:$location")
    val viewState by weeklyAdViewModel.digitalAdState

    // Enable or disable local logging
    Logger.isLoggingEnabled = true // Set to `false` to disable local logs globally

    LaunchedEffect(adService) {
        weeklyAdViewModel.fetchAdDetails(adId, location, adService)
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
                        .semantics {
                            contentDescription = "Loading weekly ad"
                            liveRegion = LiveRegionMode.Polite
                        }
                        .shimmerEffect()
                )
            }
            viewState.error != null -> {
                LaunchedEffect(viewState.error) {
                    onAdError(
                        AdErrorPayload(
                            type = AdErrorType.adLoadFailed,
                            message = viewState.error ?: "Failed to load ad",
                            isRecoverable = true
                        )
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .semantics { liveRegion = LiveRegionMode.Assertive }
                ) {
                    Text(text = "Something went wrong. Please try again.")
                    TextButton(
                        onClick = { weeklyAdViewModel.reloadWeeklyAd() },
                        // "Try Again" alone gives a screen-reader user no idea what is
                        // being retried, since the message above is a separate node.
                        modifier = Modifier.semantics {
                            contentDescription = "Try again, reload the weekly ad"
                        }
                    ) {
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
                        if (ad.pages.isEmpty()) {
                            onAdError(
                                AdErrorPayload(
                                    type = AdErrorType.adEmpty,
                                    message = "Ad loaded but contains no pages"
                                )
                            )
                        }
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
                            adService = adService,
                            zoomButtonsConfig = zoomButtonsConfig,
                            onHotSpotClick = onHotSpotClick,
                            onAdPageChanged = onAdPageChanged,
                            onAdError = onAdError
                        )
                    } else {
                        VerticalDigitalAdView(
                            modifier = Modifier.fillMaxSize(),
                            ad = ad,
                            adId = adId,
                            location = location,
                            adService = adService,
                            zoomButtonsConfig = zoomButtonsConfig,
                            onHotSpotClick = onHotSpotClick,
                            onAdError = onAdError
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
    adService: ApiService,
    zoomButtonsConfig: ZoomButtonsConfig,
    onHotSpotClick: (SpotClickPayload) -> Unit,
    onAdPageChanged: (totalPages: Int, currentPageIndex: Int, adPageId: String) -> Unit,
    onAdError: (payload: AdErrorPayload) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val actualPageCount = ad.pages.size

    // An ad with no pages would crash every `% actualPageCount` below.
    if (actualPageCount == 0) {
        Logger.e(
            "[LOG] [DigitalAd.kt] Ad has no pages, nothing to render. {adId: $adId}",
            saveLogs = null, sendToDB = false
        )
        return
    }

    // B-2: aspect ratio is tracked PER PAGE, keyed by actual page index. A single
    // shared ratio meant every page took the last-measured page's shape, and each
    // image load resized all pages at once - relayout rippling through the pager
    // while it settled is what made it creep forward on its own (B-1).
    val defaultAspectRatio = 0.826f
    val pageAspectRatios = remember { mutableStateMapOf<Int, Float>() }

    // Keyed on the loaded ad: a fresh WeeklyAd (retry/reload/new instance) drops the
    // cache, so page data is exactly as fresh as the ad it came with. Within one
    // loaded ad it stops the looping pager refetching page details on every revisit.
    val pageDetailsCache = remember(ad) { AdPageDetailsCache() }

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

    // Height responsiveness: hosts that bound the ad's height (e.g. a weighted slot
    // in a Column) used to get pages cut off, because fillMaxWidth().aspectRatio can
    // only derive height from width. When the incoming height is bounded, each page
    // is sized to fit BOTH constraints at its own aspect ratio and centered. Hotspot
    // scaling is unaffected: it is driven by the measured display size, not by an
    // assumed width. Unbounded hosts (scrollable columns) keep width-driven sizing.
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val fitToHeight = constraints.hasBoundedHeight
        val pageMaxWidth = maxWidth
        // Keep room under the pager for the page-indicator row (its paddings + dots).
        val pageMaxHeight =
            if (fitToHeight) (maxHeight - IndicatorRowHeight).coerceAtLeast(80.dp) else Dp.Unspecified

        Column(
            modifier = Modifier.fillMaxWidth()
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
                        .nestedScroll(directionalScrollConnection)
                        // Looping runs on a virtual page count of pages x 1000, which is
                        // what accessibility services would otherwise report ("item 3000
                        // of 6000"). Describe the collection with the real page count.
                        .semantics {
                            collectionInfo = CollectionInfo(
                                rowCount = 1,
                                columnCount = actualPageCount
                            )
                        },
                    verticalAlignment = Alignment.Top
                ) { virtualPageIndex ->
                    val actualPageIndex = virtualPageIndex % actualPageCount
                    val adPage = ad.pages[actualPageIndex]
                    val pageRatio = pageAspectRatios[actualPageIndex] ?: defaultAspectRatio
                    if (adPage.fileURL.isNotEmpty()) {
                        Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                        ) {
                        AdPageView(
                            adPage = adPage,
                            modifier = if (fitToHeight) {
                                Modifier
                                    .width(min(pageMaxWidth, pageMaxHeight * pageRatio))
                                    .aspectRatio(pageRatio)
                            } else {
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(pageRatio)
                            },
                            adId = adId,
                            location = location,
                            adService = adService,
                            pageDetailsCache = pageDetailsCache,
                            onHotSpotClick = onHotSpotClick,
                            key = virtualPageIndex,
                            saveLogEnabled = ad.isLogEnabled,
                            isScrollable = false,
                            onAdError = onAdError,
                            onSizeCalculated = { size ->
                                if (size.width > 0 && size.height > 0) {
                                    val newRatio = size.width / size.height
                                    val current = pageAspectRatios[actualPageIndex]
                                    // Epsilon compare: onGloballyPositioned re-reports on
                                    // every relayout with float jitter, and an exact !=
                                    // kept the state churning forever.
                                    if (current == null || abs(current - newRatio) > 0.01f) {
                                        pageAspectRatios[actualPageIndex] = newRatio
                                    }
                                }
                            }
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
}

@Composable
internal fun VerticalDigitalAdView(
    modifier: Modifier = Modifier,
    ad: WeeklyAd,
    adId: String,
    location: String,
    adService: ApiService,
    zoomButtonsConfig: ZoomButtonsConfig,
    onHotSpotClick: (SpotClickPayload) -> Unit,
    onAdError: (payload: AdErrorPayload) -> Unit
) {
    val pageDetailsCache = remember(ad) { AdPageDetailsCache() }

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
                                    adService = adService,
                                    pageDetailsCache = pageDetailsCache,
                                    onHotSpotClick = onHotSpotClick,
                                    key = index,
                                    saveLogEnabled = ad.isLogEnabled,
                                    isScrollable = false,
                                    onAdError = onAdError
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
// Dot + gap is the distance between tap targets. 24.dp is the WCAG 2.5.8 minimum; the
// original 8.dp gap left 16.dp targets, under it. The band stays visually tight at this
// pitch - 48.dp slots met Android's larger guideline but spread the dots far wider than
// the client's design allows.
private val IndicatorDotSpacing = 16.dp
// Each dot's tap target: as wide as the pitch, and tall enough to be comfortable. The row
// reserves IndicatorRowHeight below the pager, so height-bounded hosts still fit a page.
private val IndicatorTouchHeight = 44.dp
internal val IndicatorRowHeight = IndicatorTouchHeight + 24.dp

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

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {

    val dotRadiusPx = with(LocalDensity.current) { dotSize.toPx() / 2f }
    val slotWidth = dotSize + dotSpacing
    // 48.dp slots are wide, so on a narrow host only so many fit. Showing fewer dots is
    // better than overflowing the ad's width.
    val fitCount =
        if (constraints.hasBoundedWidth) (maxWidth / slotWidth).toInt().coerceAtLeast(1)
        else maxVisibleDots
    val visibleCount = minOf(pageCount, maxVisibleDots.coerceAtLeast(1), fitCount)
    // The band is the block of pages the current page falls in. The trailing block is
    // pulled back so it stays full width instead of rendering a stub of a few dots.
    val windowStart = (currentPage / visibleCount * visibleCount)
        .coerceIn(0, pageCount - visibleCount)
    val windowEnd = windowStart + visibleCount - 1
    val hasMoreBefore = windowStart > 0
    val hasMoreAfter = windowEnd < pageCount - 1

    val slotPx = with(LocalDensity.current) { slotWidth.toPx() }

    // Accessibility services grow any target smaller than the view configuration's
    // minimum (48.dp) and then clip it against its neighbour. With dots this close that
    // shifted every dot's REPORTED bounds half a slot sideways, so the screen reader's
    // focus rectangle sat between two dots. Telling this subtree that a dot-sized target
    // is the minimum stops the expansion, and the dots report the bounds they are drawn
    // at - without widening the band.
    val viewConfiguration = LocalViewConfiguration.current
    val dotViewConfiguration = remember(viewConfiguration, slotWidth) {
        object : ViewConfiguration by viewConfiguration {
            override val minimumTouchTargetSize: DpSize = DpSize(slotWidth, IndicatorTouchHeight)
        }
    }

    CompositionLocalProvider(LocalViewConfiguration provides dotViewConfiguration) {
    Row(
        // The row itself carries the live page status. Screen readers announce it on every
        // page change - swiping the pager used to be completely silent - and a user landing
        // here hears where they are before stepping through the dots.
        modifier = Modifier
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = "Page ${currentPage + 1} of $pageCount"
            }
            // Taps are handled here, for the whole strip, rather than per dot. A pointer
            // input modifier on a dot gets its touch bounds grown to the 48.dp minimum;
            // neighbouring dots then overlap and each dot's REPORTED bounds slide half a
            // slot sideways, which is why the screen reader's focus rectangle sat between
            // two dots. Without pointer input of their own, the dots report exactly the
            // bounds they are drawn at.
            .pointerInput(windowStart, visibleCount, slotPx) {
                detectTapGestures { tap ->
                    val slot = (tap.x / slotPx).toInt().coerceIn(0, visibleCount - 1)
                    onPageSelected(windowStart + slot)
                }
            },
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

            val isCurrent = index == currentPage

            // ONE node per dot: the touch target, the label, the state, the action and
            // the drawn dot itself. Any extra composable inside this Box becomes a second
            // accessibility node, and the two nodes' bounds end up half a slot apart - the
            // focus rectangle then lands between two dots instead of on the focused one,
            // which is what the client saw after the first ADA pass. Drawing the dot
            // instead of nesting a Box keeps the node, the tap area and the pixels aligned.
            Box(
                modifier = Modifier
                    // Keeps the tap target (and the pitch between dots) constant while
                    // the dot itself scales.
                    .width(dotSize + dotSpacing)
                    .height(IndicatorTouchHeight)
                    .semantics(mergeDescendants = true) {
                        role = Role.Tab
                        // selected is what tells a screen reader WHICH page is current;
                        // without it every dot read identically.
                        selected = isCurrent
                        contentDescription = "Page ${index + 1} of $pageCount"
                        onClick(label = "Go to page ${index + 1}") {
                            onPageSelected(index)
                            true
                        }
                    }
                    .pointerInput(index) {
                        detectTapGestures { onPageSelected(index) }
                    }
                    .drawBehind {
                        drawCircle(
                            color = color,
                            radius = dotRadiusPx * scale,
                            center = this.center
                        )
                    }
            )
        }
    }
    }
    }
}
