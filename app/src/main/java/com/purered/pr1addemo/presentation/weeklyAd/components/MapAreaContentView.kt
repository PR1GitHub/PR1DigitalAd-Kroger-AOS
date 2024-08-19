package com.purered.pr1addemo.presentation.weeklyAd.components

import android.os.Build
import android.transition.CircularPropagation
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.purered.pr1addemo.data.repository.AdPage
import com.purered.pr1addemo.data.repository.weeklyAdService
import com.purered.pr1addemo.presentation.Dimens
import com.purered.pr1addemo.presentation.common.shimmerEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.purered.pr1addemo.data.repository.OfferDetails
import java.time.format.DateTimeFormatter
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.*

fun parseHtmlString(input: String): AnnotatedString {

    try {
        val formattedString = input
            .replace("<br>", "")
            .replace("<BR>", "")
            .replace("\\n", "")
            .replace("<font size=\"4\">", "")
            .replace("</font>", "")

        return AnnotatedString(formattedString)
    }catch (e: Exception) {
        println("Error parsing date")
        return AnnotatedString("")
    }

}

fun formatEventDates(startDateString: String?, endDateString: String?): String {



    try {

        if (startDateString == null || endDateString == null) {
            return ""
        }

        // Define the input and output date formats
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val outputMonthFormat = SimpleDateFormat("MMMM", Locale.US)
        val outputDayFormat = SimpleDateFormat("d", Locale.US)
        val outputYearFormat = SimpleDateFormat("yyyy", Locale.US)

        // Parse the date strings into Date objects
        val startDate = inputFormat.parse(startDateString)
        val endDate = inputFormat.parse(endDateString)

        // Format the start and end dates
        val startMonth = outputMonthFormat.format(startDate)
        val startDay = outputDayFormat.format(startDate).toInt()
        val endMonth = outputMonthFormat.format(endDate)
        val endDay = outputDayFormat.format(endDate).toInt()
        val year = outputYearFormat.format(endDate)

        // Get the day suffixes
        val startDaySuffix = getDayOfMonthSuffix(startDay)
        val endDaySuffix = getDayOfMonthSuffix(endDay)

        return "$startMonth $startDay$startDaySuffix - $endMonth $endDay$endDaySuffix, $year"

    }catch (e: Exception) {
        println("Error parsing date")
        return ""
    }
}

fun getDayOfMonthSuffix(day: Int): String {
    return when (day) {
        1, 21, 31 -> "st"
        2, 22 -> "nd"
        3, 23 -> "rd"
        else -> "th"
    }
}



@Composable
fun MapAreaContentView(
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
                        model = offerDetails?.imageURL,
                        contentDescription = null,
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




