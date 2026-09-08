package com.purered.pr1digitalad.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

import com.purered.pr1digitaladclassic.AdErrorPayload
import com.purered.pr1digitaladclassic.AdExperience
import com.purered.pr1digitaladclassic.ApiEnv
import com.purered.pr1digitaladclassic.DigitalAd
import com.purered.pr1digitaladclassic.SpotClickPayload
import com.purered.pr1digitaladclassic.ZoomButtonsConfig

internal const val DEMO_AD_ID = "649956ed-3ed4-4d68-b388-aa864a7668e8"
internal const val DEMO_LOC_ID = "01800364"
internal const val DEMO_STAGING_KEY = "pgH7QzFHJx4w46fI~5Uzi4RvtTwlEXp2"

/*
 * Landscape harness replicating the client host-app design around the SDK: title bar
 * with back/search/overflow, a location chip, then a two-column layout - the ad (the
 * library) in a 60% column and an offers rail in the remaining 40%, fed live from
 * onHotSpotClick payloads. The last onAdError is shown in the rail so error flows
 * stay visible in landscape too. Everything here is host-side; the library only
 * receives a 60%-width slot. Shared by the Weekly Ad and One Ad screens.
 */
@Composable
internal fun LandscapeAdHarness(
    modifier: Modifier = Modifier,
    title: String,
    logTag: String,
    adExperience: AdExperience = AdExperience.oneAd,
    adId: String = DEMO_AD_ID,
    locationId: String = DEMO_LOC_ID,
    apiKey: String = DEMO_STAGING_KEY
) {
    var totalPages by remember { mutableStateOf(0) }
    var currentPage by remember { mutableStateOf(1) }
    var lastError by remember { mutableStateOf<AdErrorPayload?>(null) }
    val offers = remember { mutableStateListOf<SpotClickPayload>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            // The shell is edge-to-edge; this custom chrome must clear the status bar
            // and any landscape camera cutout itself.
            .statusBarsPadding()
            .displayCutoutPadding()
    ) {
        // Title bar: back, left-aligned title + ad dates, search + overflow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("<Ad Dates>", fontSize = 11.sp, color = Color(0xFF8A8A8A))
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More")
            }
        }
        HorizontalDivider(color = Color(0xFFEEEEEE))

        Row(modifier = Modifier.fillMaxSize()) {

            // ---- Ad column: 60%, the SDK's slot ----
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF1A73E8),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "Location",
                        fontSize = 12.sp,
                        color = Color(0xFF1A73E8),
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    DigitalAd(
                        modifier = Modifier.fillMaxWidth(),
                        adId = adId,
                        location = locationId,
                        apiEnv = ApiEnv.QA,
                        apiKey = apiKey,
                        adExperience = adExperience,
                        zoomButtonsConfig = ZoomButtonsConfig(enable = false),
                        onAdLoaded = { pages -> totalPages = pages },
                        onAdPageChanged = { pages, pageIndex, _ ->
                            totalPages = pages
                            currentPage = pageIndex + 1
                        },
                        onHotSpotClick = { payload ->
                            Log.d(logTag, "onHotSpotClick: $payload")
                            if (offers.none { it.id == payload.id }) {
                                offers.add(0, payload)
                            }
                        },
                        onAdError = { error ->
                            Log.e(logTag, "onAdError: $error")
                            lastError = error
                        }
                    )
                }
            }

            // ---- Offers rail: 40% ----
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${offers.size} offers on page $currentPage of $totalPages",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                lastError?.let {
                    Text(
                        text = "Error: ${it.type} - ${it.message}" +
                            (it.adPageId?.let { id -> " (page $id)" } ?: ""),
                        fontSize = 11.sp,
                        color = Color(0xFFB00020),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                if (offers.isEmpty()) {
                    Text(
                        "Tap an offer in the ad to collect it here.",
                        fontSize = 12.sp,
                        color = Color(0xFF8A8A8A)
                    )
                }
                LazyColumn {
                    items(offers, key = { it.id }) { offer ->
                        OfferCard(offer)
                        HorizontalDivider(color = Color(0xFFF2F2F2))
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferCard(offer: SpotClickPayload) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = offer.imageURL,
            contentDescription = offer.headline,
            modifier = Modifier.size(52.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            val price = offer.pricingText.ifBlank { offer.pricingHTML }
            if (price.isNotBlank()) {
                Text(price, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Text(offer.headline, fontSize = 12.sp, maxLines = 2)
            if (offer.bodyCopy.isNotBlank()) {
                Text(offer.bodyCopy, fontSize = 10.sp, color = Color(0xFF8A8A8A), maxLines = 1)
            }
        }
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(30.dp)
        ) {
            Text("Shop Deal", fontSize = 11.sp)
        }
    }
}
