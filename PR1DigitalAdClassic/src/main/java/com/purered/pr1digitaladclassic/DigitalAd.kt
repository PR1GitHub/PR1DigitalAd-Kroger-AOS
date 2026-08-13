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
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            viewState.loading -> {
                Logger.i("[API-LOG]  Loading...", saveLogs = null, sendToDB = false)
                CircularProgressIndicator(
                    progress = {0.85F},
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
                    //WeeklyAdItem(ad = viewState.WeeklyAd!!)

                    val ad = viewState.weeklyAd!!

                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId, loc = location,
                        appDetails = "AOS:[LOG] [DigitalAd.kt]  WeeklyAd data fetched. {adId: $adId, location: $location}"
                    ))
                    Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = ad.isLogEnabled)


                    Column(modifier = Modifier.fillMaxSize()) {

                        val coroutineScope = rememberCoroutineScope()
                        val pagerState = if (isHorizontalView) {
                            rememberPagerState(pageCount = { ad.pages.size })
                        } else {
                            null
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            ZoomableBoxContent(
                                content = {
                                    if (isHorizontalView && pagerState != null) {
                                        HorizontalPager(
                                            state = pagerState,
                                            modifier = Modifier.fillMaxSize()
                                        ) { pageIndex ->
                                            val adPage = ad.pages[pageIndex]
                                            if (adPage.fileURL.isNotEmpty()) {
                                                AdPageView(
                                                    adPage = adPage,
                                                    modifier = Modifier.fillMaxSize(),
                                                    adId = adId,
                                                    location = location,
                                                    onHotSpotClick = onHotSpotClick,
                                                    key = pageIndex,
                                                    saveLogEnabled = ad.isLogEnabled
                                                )
                                            }
                                        }
                                    } else {
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

                        if (isHorizontalView && pagerState != null) {
                            PagerIndicators(
                                pageCount = ad.pages.size,
                                currentPage = pagerState.currentPage,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = 16.dp),
                                onPageSelected = { index ->
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            )
                        }
                    }
                }
            }

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
