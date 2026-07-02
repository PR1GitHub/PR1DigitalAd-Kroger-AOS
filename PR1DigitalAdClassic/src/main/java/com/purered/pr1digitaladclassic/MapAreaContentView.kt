package com.purered.pr1digitaladclassic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

@Composable
internal fun MapAreaContentView(
    modifier: Modifier,
    offerDetails: OfferDetails, eventId: Int,
    onClose: () -> Unit
) {



    //  var loadingState by remember { mutableStateOf<String>("idle") }

//    LaunchedEffect(mapArea) {
//
//
//        try {
//            loadingState = "loading"
//            if (mapAreaContent?.offerVersionProductGroupId != null) {
//                val offerDetailsList = weeklyAdService.getOfferDetails(
//                    eventId,
//                    mapAreaContent.offerVersionProductGroupId.toInt()
//                );
//                if (offerDetailsList.isNotEmpty()) {
//                    offerDetails = offerDetailsList[0]
//
//                    loadingState = "success"
//                } else {
//                    loadingState = "error"
//                }
//            }
//
//        } catch (
//            e: Exception
//        ) {
//            loadingState = "error"
//            println("Error fetching offer details ${e.message}")
//        }
//
//
//    }



    Column(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .verticalScroll(
                rememberScrollState()
            )
    ) {

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = Dimens.MediumPadding1)
        ) {
            if (offerDetails?.imageURL != null) {

                SubcomposeAsyncImage(
                    model = offerDetails.imageURL,
                    contentDescription = offerDetails.headline?.takeIf { it.isNotBlank() }
                        ?: "Offer image",
                    loading = {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(100.dp)
                                .padding(horizontal = Dimens.MediumPadding1)
                                .shimmerEffect()
                        )

                    },
                    error = { BasicText(text = "Error loading image") },
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                        .align(Alignment.Center)
                )


            } else {
                Text(
                    "No Image Available",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            formatEventDates(offerDetails?.startDate, offerDetails?.endDate),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            parseHtmlString(offerDetails?.pricingTemplateName ?: ""),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            offerDetails?.headline ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            offerDetails?.bodyCopy ?: "",
            style = MaterialTheme.typography.bodySmall
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onClose) {
                Text("Add to Cart" , color = Color.White)
            }
            Button(onClick = onClose) {
                Text("Shop Details")
            }

        }

        Text(
            (offerDetails?.offerDisclaimer ?: "" )
                    + (offerDetails?.eventDisclaimer ?: "")
                    +"In the event of disagreement between the flyer and this popup, the flyer shall take precedence."
                    +(offerDetails?.genericDisclaimer ?: "")
            ,
            style = MaterialTheme.typography.labelSmall
        )
        // You can add an image here if you have one
        // Image(modifier = Modifier.fillMaxWidth(), ...)
    }


}