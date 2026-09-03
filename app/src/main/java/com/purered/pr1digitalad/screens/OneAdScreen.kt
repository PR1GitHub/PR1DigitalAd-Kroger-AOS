package com.purered.pr1digitalad.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp

import com.purered.pr1digitaladclassic.AdExperience
import com.purered.pr1digitaladclassic.ApiEnv
import com.purered.pr1digitaladclassic.DigitalAd
import com.purered.pr1digitaladclassic.DigitalAdLibVersion
import com.purered.pr1digitaladclassic.SpotClickPayload
import com.purered.pr1digitaladclassic.ZoomButtonsConfig
import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneAdScreen(
    modifier: Modifier = Modifier
) {
    var totalPages by remember { mutableStateOf(0) }
    var currentPageIndex by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("One Ad")
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
            horizontalAlignment = Alignment.Start
        ) {
            // OneAd composable to display the ad
            OneAd(
                modifier = Modifier
                    .fillMaxWidth()
                    //.height(500.dp)
                    .weight(1f)
                    //.shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                    //.clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F5F5)),
                adExperience = AdExperience.oneAd,
                onAdLoadedTrigger = { pages ->
                    totalPages = pages
                },
                onPageChangedTrigger = { pageIndex ->
                    currentPageIndex = pageIndex
                }
            )

            // Page indicator for the ad
            Text(
                modifier = Modifier.padding(all = 10.dp),
                fontSize = 16.sp,
                text = "page ${currentPageIndex + 1} of $totalPages",
            )
        }
    }
}

@Composable
fun OneAd(
    modifier: Modifier = Modifier,
    viewHeight: Dp? = null,
    adExperience: AdExperience,
    onAdLoadedTrigger: (Int) -> Unit = {},
    onPageChangedTrigger: (Int) -> Unit = {}
) {

    var adId = "649956ed-3ed4-4d68-b388-aa864a7668e8"
    var locId = "01800364"

    var stagingKey = "pgH7QzFHJx4w46fI~5Uzi4RvtTwlEXp2"
    var prodKey = "bqwwosbzrzcvffztxzyczieljzsahmkp"

    Log.i("PR1DigitalAd-AOS-SDK", "PR1DigitalAd Version = v$DigitalAdLibVersion");

    // State for showing dialog + holding payload
    var showDialog by remember { mutableStateOf(false) }
    var dialogPayload by remember { mutableStateOf<SpotClickPayload?>(null) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        DigitalAd(
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp, end = 10.dp),
            adId = adId,
            location = locId,
            apiEnv = ApiEnv.QA,
            apiKey = stagingKey,
            adExperience = adExperience,
            zoomButtonsConfig = ZoomButtonsConfig(enable = false, offsetY = -10),
            onAdLoaded = { totalPages: Int ->
                onAdLoadedTrigger(totalPages)

                Log.d("OneAdScreen", "Ad Loaded Successfully with total pages: $totalPages")
            },
            onAdPageChanged = {  totalPages: Int, currentPageIndex: Int, adPageId: String ->
                onPageChangedTrigger(currentPageIndex)

                Log.d("OneAdScreen", "Ad Page Changed to: $currentPageIndex")
            },
            onHotSpotClick = { payload: SpotClickPayload ->
                if (payload.itemType == "promo") {
                    // Handle content type Creative
                    Log.d("Promo Payload -->", payload.toString());
                }

                if (payload.itemType == "offer") {
                    // Handle content type Offer
                    Log.d("Offer Payload -->", payload.toString());
                }

                // Show dialog
                dialogPayload = payload
                showDialog =
                    true // make true to visually see the payload in a alert box (for dev only)
            }
        )
    }

    // Compose AlertDialog
    if (showDialog && dialogPayload != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(text = "Hotspot Clicked")
            },
            text = {
                Text(text = dialogPayload.toString())
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}