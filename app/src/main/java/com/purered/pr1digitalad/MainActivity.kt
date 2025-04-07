package com.purered.pr1digitalad

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.purered.pr1digitalad.ui.theme.PR1DigitalAdTheme
import com.purered.pr1digitaladclassic.ApiEnv


import com.purered.pr1digitaladclassic.DigitalAd
import com.purered.pr1digitaladclassic.DigitalAdLibVersion
import com.purered.pr1digitaladclassic.SpotClickPayload

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            PR1DigitalAdTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
    var adId_12 = "0614c2fb-3279-4044-8d45-21faf406bd71"
    var locId_12 = "02100537"

    var adId_14 = "a2010c61-380c-49d4-acdd-28fe2b3c519b"
    var locId_14 = "01100002"

    var adId_15 = "eb114a9f-7856-4364-9d81-7ebe7356e603"
    var locId_15 = "01100002"

    var adId_16 = "3b52262f-8168-4815-8d91-14408edf69d2"
    var locId_16 = "01100002"

    var adId_17 = "9AA728AE-EFBC-4B5E-9473-462FF5094F21"
    var locId_17 = "01100352"

    var adId_18 = "CC41D863-BF35-4317-85E1-B1736CA31893"
    var locId_18 = "01100002"

//    var adId = "71fbaa33-c8a2-4c73-974e-190a27bdcba3"
//    var locId = "02100724"

    //9300cb13-e559-4397-94c5-ab8f46e3e367?location=02100724
//    var adId = "9300cb13-e559-4397-94c5-ab8f46e3e367"
//    var locId = "02100724"

//    var adId = "84E1A491-D9CA-4A13-BD5A-CC14F8516646"
//    var locId = "01600128"

//    var adId = "ebaa53d3-840f-4772-84d0-464e03ace4d5"
//    var locId = "01400367"

//    var adId = "bff71115-e620-4dc6-b9c1-14c4b885424d"
//    var locId = "02100537"

    var adId = "3e41d207-818b-4e65-a07e-8079608b2d2f"
    var locId = "02100537"

    // QA
    var adId_13 = "2d2514de-bd6c-42b7-aacd-43e1ac19bf1d"
    var locId_13 = "01600128"

    var stagingKey = "pgH7QzFHJx4w46fI~5Uzi4RvtTwlEXp2"
    var prodKey = "bqwwosbzrzcvffztxzyczieljzsahmkp"

    Log.i("PR1DigitalAd-AOS-SDK","PR1DigitalAd Version = v$DigitalAdLibVersion");

    DigitalAd(
        adId = adId,
        location = locId,
        apiEnv = ApiEnv.PROD,
        apiKey = prodKey,
        onHotSpotClick = {  payload:SpotClickPayload ->
            if (payload.itemType == "promo") {
                // Handle content type Creative
                Log.d("Promo Payload -->", payload.toString());
            }

            if (payload.itemType == "offer") {
                // Handle content type Offer
                Log.d("Offer Payload -->", payload.toString());
            }
        })













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