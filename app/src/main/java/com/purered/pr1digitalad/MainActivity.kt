package com.purered.pr1digitalad

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.purered.pr1digitalad.ui.theme.PR1DigitalAdTheme
import com.purered.pr1digitaladclassic.ApiEnv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api


import com.purered.pr1digitaladclassic.DigitalAd
import com.purered.pr1digitaladclassic.DigitalAdLibVersion
import com.purered.pr1digitaladclassic.SpotClickPayload
import com.purered.pr1digitaladclassic.ZoomButtonsConfig

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            PR1DigitalAdTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text("Weekly Ad Demo")
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.LightGray,
                                titleContentColor = Color.Black
                            )
                        )
                    }
                ) { innerPadding ->
                    WeeklyAdScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}


@Composable
fun WeeklyAdScreen(
    modifier: Modifier = Modifier) {

    // PROD
    var adId = "c9e1dae8-f3c4-4178-8a31-bace4546619b"
    var locId = "02100537"

    var stagingKey = "pgH7QzFHJx4w46fI~5Uzi4RvtTwlEXp2"
    var prodKey = "bqwwosbzrzcvffztxzyczieljzsahmkp"

    Log.i("PR1DigitalAd-AOS-SDK","PR1DigitalAd Version = v$DigitalAdLibVersion");

    // State for showing dialog + holding payload
    var showDialog by remember { mutableStateOf(false) }
    var dialogPayload by remember { mutableStateOf<SpotClickPayload?>(null) }


    Box(
        modifier = modifier.fillMaxSize()
    ) {
        DigitalAd(
            adId = adId,
            location = locId,
            apiEnv = ApiEnv.PROD,
            apiKey = prodKey,
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
                showDialog = true // make true to visually see the payload in a alert box (for dev only)
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













//    val eventId = 12032 // Replace with dynamic value
//
//    val mode = "public"
//    val weeklyAdViewModel: WeeklyAdsViewModel = viewModel()
//    val viewState by weeklyAdViewModel.weeklyAdState
//
//
//    LaunchedEffect(Unit) {
//        weeklyAdViewModel.fetchWeeklyAd(eventId, mode)
//    }
//    Box(modifier = Modifier.fillMaxSize()) {
//        when {
//            viewState.loading -> {
//                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//            }
//            viewState.error != null -> {
//                Column(modifier = Modifier.align(Alignment.Center)) {
//                    Text(text = "Something went wrong. Please try again.")
//                    TextButton(onClick = { weeklyAdViewModel.reloadWeeklyAd() }) {
//                        Text("Try Again")
//                    }
//                }
//
//            }
//            else -> {
//                if(viewState.WeeklyAd != null)
//                WeeklyAdItem(ad = viewState.WeeklyAd!!)
//            }
//        }
//    }


}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PR1DigitalAdTheme {
        Greeting("Android")
    }
}