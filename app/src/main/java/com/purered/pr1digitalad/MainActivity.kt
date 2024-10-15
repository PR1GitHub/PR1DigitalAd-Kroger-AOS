package com.purered.pr1digitalad

import android.os.Bundle
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
import com.purered.pr1digitaladclassic.SpotClickPayload

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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


    DigitalAd(
        adId = "8d7d7bfd-9dd1-4738-b98f-cb1ec10e3026",
        location =  "01800775",
        apiEnv = ApiEnv.PROD,
        apiKey = "bqwwosbzrzcvffztxzyczieljzsahmkp",
        onHotSpotClick = {  payload:SpotClickPayload ->



            if (payload.itemType == "promo") {
                // Handle content type Creative
            }

            if (payload.itemType == "offer") {
                // Handle content type Offer
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