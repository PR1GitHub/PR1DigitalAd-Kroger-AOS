package com.purered.pr1digitalad.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

import com.purered.pr1digitaladclassic.AdExperience
import com.purered.pr1digitaladclassic.ApiEnv
import com.purered.pr1digitaladclassic.DigitalAd
import com.purered.pr1digitaladclassic.DigitalAdLibVersion
import com.purered.pr1digitaladclassic.SpotClickPayload
import com.purered.pr1digitaladclassic.ZoomButtonsConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyAdScreen(
    modifier: Modifier = Modifier
) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeAdHarness(
            modifier = modifier,
            title = "Weekly Ad",
            logTag = "WeeklyAdScreen"
        )
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Weekly Ad")
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        ClassicAd(
            modifier = Modifier.padding(innerPadding),
            adExperience = AdExperience.classic
        )
    }
}

@Composable
fun ClassicAd(
    modifier: Modifier = Modifier,
    adExperience: AdExperience,
    ) {

    Log.i("PR1DigitalAd-AOS-SDK", "PR1DigitalAd Version = v$DigitalAdLibVersion")

    // State for showing dialog + holding payload
    var showDialog by remember { mutableStateOf(false) }
    var dialogPayload by remember { mutableStateOf<SpotClickPayload?>(null) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        DigitalAd(
            adId = DEMO_AD_ID,
            location = DEMO_LOC_ID,
            apiEnv = ApiEnv.QA,
            apiKey = DEMO_STAGING_KEY,
            adExperience = adExperience,
            zoomButtonsConfig = ZoomButtonsConfig(enable = false, offsetY = -10),
            onHotSpotClick = { payload: SpotClickPayload ->
                if (payload.itemType == "promo") {
                    // Handle content type Creative
                    Log.d("Promo Payload -->", payload.toString())
                }

                if (payload.itemType == "offer") {
                    // Handle content type Offer
                    Log.d("Offer Payload -->", payload.toString())
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
