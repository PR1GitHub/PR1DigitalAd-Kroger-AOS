package com.purered.pr1digitaladclassic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log

val DigitalAdLibVersion = "0.0.7" // This is the version of the DigitalAd library

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
)

@Composable
fun DigitalAd(
    adId:String,
    location:String,
    apiKey:String,
    apiEnv: ApiEnv,
    onHotSpotClick: ( payload:SpotClickPayload) -> Unit
) {

    weeklyAdService = createWeeklyAdService(apiEnv,apiKey)

    val mode = "public"
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
                    progress = 0.85F,
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
                Logger.e("${logData.value.appDetails}", saveLogs = logData, sendToDB = true)
            }
            else -> {
                if(viewState.WeeklyAd != null) {
                    //WeeklyAdItem(ad = viewState.WeeklyAd!!)

                    val ad = viewState.WeeklyAd!!;

                    val logData = SaveLogs(SaveLogDetails(
                        adId = adId, loc = location,
                        appDetails = "AOS:[LOG] [DigitalAd.kt]  WeeklyAd data fetched. {adId: $adId, location: $location}"
                    ))
                    Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = ad.isLogEnabled)


                    Box(modifier = Modifier.fillMaxSize()
                    ) {

                        ZoomableBoxContent {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Add verticalScroll modifier
                                ad.pages.forEachIndexed { index, adPage ->
                                    // Pass each AdPage to a separate AdPageView
                                    if(adPage.fileURL.isNotEmpty() == true)
                                        AdPageView(
                                            adPage = adPage,
                                            modifier = Modifier,
                                            adId = adId,
                                            location = location,
                                            onHotSpotClick = onHotSpotClick,
                                            key = index,
                                            saveLogEnabled = ad.isLogEnabled
                                        )
                                }

                                val logData = SaveLogs(SaveLogDetails(
                                    adId = adId, loc = location,
                                    appDetails = "AOS:[LOG] [DigitalAd.kt]  Generating AdPageView... {adId: $adId, location: $location}"
                                ))
                                Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = ad.isLogEnabled)
                            }
                        }
                    }
                }
            }

        }
    }

}
