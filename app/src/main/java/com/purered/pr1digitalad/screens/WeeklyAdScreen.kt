package com.purered.pr1digitalad.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import com.purered.pr1digitaladclassic.ApiEnv
import com.purered.pr1digitaladclassic.DigitalAd
import com.purered.pr1digitaladclassic.DigitalAdLibVersion
import com.purered.pr1digitaladclassic.SpotClickPayload
import com.purered.pr1digitaladclassic.ZoomButtonsConfig
import com.purered.pr1digitaladclassic.AdExperience

import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyAdScreen(
    modifier: Modifier = Modifier
) {
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
            adId = adId,
            location = locId,
            apiEnv = ApiEnv.QA,
            apiKey = stagingKey,
            adExperience = adExperience,
            zoomButtonsConfig = ZoomButtonsConfig(enable = false, offsetY = -10),
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