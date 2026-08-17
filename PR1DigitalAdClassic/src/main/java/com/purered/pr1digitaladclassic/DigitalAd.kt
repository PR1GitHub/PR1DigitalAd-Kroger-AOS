package com.purered.pr1digitaladclassic

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    isHorizontalView: Boolean,
    //zoomControls: Boolean = true,
    //zoomControlsOffset: Int = -140,
    zoomButtonsConfig: ZoomButtonsConfig = ZoomButtonsConfig(),
    onHotSpotClick: ( payload:SpotClickPayload) -> Unit
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
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
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

                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId, loc = location,
                        appDetails = "AOS:[LOG] [DigitalAd.kt]  WeeklyAd data fetched. {adId: $adId, location: $location}"
                    ))
                    Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = ad.isLogEnabled)

                    if (isHorizontalView) {
                        HorizontalDigitalAdView(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            ad = ad,
                            adId = adId,
                            location = location,
                            zoomButtonsConfig = zoomButtonsConfig,
                            onHotSpotClick = onHotSpotClick
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
    onHotSpotClick: (SpotClickPayload) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val actualPageCount = ad.pages.size

    // Looping behavior: Use a large virtual page count and modulo for actual content
    val loopingFactor = 1000
    val virtualPageCount = if (actualPageCount > 1) actualPageCount * loopingFactor else actualPageCount
    val initialPage = if (actualPageCount > 1) (virtualPageCount / 2) else 0

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { virtualPageCount }
    )

    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            ZoomableBoxContent(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                content = {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    ) { virtualPageIndex ->
                        val actualPageIndex = virtualPageIndex % actualPageCount
                        val adPage = ad.pages[actualPageIndex]
                        if (adPage.fileURL.isNotEmpty()) {
                            AdPageView(
                                adPage = adPage,
                                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                adId = adId,
                                location = location,
                                onHotSpotClick = onHotSpotClick,
                                key = virtualPageIndex,
                                saveLogEnabled = ad.isLogEnabled
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
        }

        PagerIndicators(
            pageCount = actualPageCount,
            currentPage = pagerState.currentPage % actualPageCount,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp),
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

@Composable
internal fun PagerIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    onPageSelected: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val color = if (currentPage == index) Color.DarkGray else Color.LightGray
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onPageSelected(index) }
            )
        }
    }
}
