package com.purered.pr1digitalad


import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.purered.pr1digitalad.ui.theme.PR1DigitalAdTheme
import com.purered.pr1digitaladclassic.AdExperience
import com.purered.pr1digitaladclassic.ApiEnv
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
                var adExperience by remember { mutableStateOf(AdExperience.oneAd) }
                val backgroundColor = if (adExperience == AdExperience.oneAd) Color(0xFFC0C0C0) else Color.White

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    containerColor = backgroundColor,
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text("Weekly Ad Demo")
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.LightGray,
                                titleContentColor = Color.Black
                            ),
                            actions = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = if (adExperience == AdExperience.oneAd) "Horizontal" else "Vertical",
                                        color = Color.Black,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Switch(
                                        checked = adExperience == AdExperience.oneAd,
                                        onCheckedChange = { isChecked ->
                                            adExperience = if (isChecked) AdExperience.oneAd else AdExperience.classic
                                        }
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    if (adExperience == AdExperience.oneAd) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(10.dp),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            WeeklyAdScreen(
                                adExperience = adExperience,
                                viewHeight = null
                            )
                        }
                    } else {
                        WeeklyAdScreen(
                            adExperience = adExperience,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
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
    adExperience: AdExperience,
    modifier: Modifier = Modifier,
    viewHeight: Dp? = null
) {

    val context = LocalContext.current

    // PROD
    val adId = "8fff1a9e-219d-4bee-b6ff-9e9a4c5a231a"
    val locId = "70100005"

    val stagingKey = "pgH7QzFHJx4w46fI~5Uzi4RvtTwlEXp2"
    val prodKey = "bqwwosbzrzcvffztxzyczieljzsahmkp"

    Log.i("PR1DigitalAd-AOS-SDK", "PR1DigitalAd Version = v$DigitalAdLibVersion")

    // State for showing dialog + holding payload
    var showDialog by remember { mutableStateOf(false) }
    var dialogPayload by remember { mutableStateOf<SpotClickPayload?>(null) }
    var currentPageId by remember { mutableStateOf("") }
    var totalPageCount by remember { mutableStateOf(0) }
    var pagePosition by remember { mutableStateOf(0) }


    val boxModifier = if (adExperience == AdExperience.oneAd) {
        if (viewHeight != null) {
            modifier.fillMaxWidth().height(viewHeight)
        } else {
            modifier.fillMaxWidth().wrapContentHeight()
        }
    } else {
        modifier.fillMaxSize()
    }

    Box(modifier = boxModifier) {
        DigitalAd(
            modifier = if (adExperience == AdExperience.oneAd && viewHeight == null) Modifier.fillMaxWidth().wrapContentHeight().animateContentSize()
                       else Modifier.fillMaxSize(),
            adId = adId,
            location = locId,
            apiEnv = ApiEnv.QA,
            apiKey = stagingKey,
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
                showDialog = true // make true to visually see the payload in a alert box (for dev only)
            },
            onCompleteAdLoad = { pageCount: Int ->
                totalPageCount = pageCount
                Toast.makeText(context, "Pages: $pageCount", Toast.LENGTH_SHORT).show()
            },
            didChangeAdPage = {
                    currentPagePosition: Int, pageCount: Int, adPageId: String ->
                currentPageId = adPageId
                totalPageCount = pageCount
                pagePosition = currentPagePosition
                //Toast.makeText(context, "Page: $eventPageId, $currentPage/$pageCount", Toast.LENGTH_SHORT).show()
            }

        )
    }

    if (adExperience == AdExperience.oneAd){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
            ) {
                Text(text = "Page Id: $currentPageId")
                Text(text = "Page: $pagePosition/$totalPageCount")
            }
        }
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