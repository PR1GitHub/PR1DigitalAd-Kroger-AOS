package com.purered.pr1digitaladclassic

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object Dimens {

    val ExtraSmallPadding = 3.dp
    val ExtraSmallPadding2 = 6.dp
    val MediumPadding1 = 24.dp
    val MediumPadding2 = 30.dp
    val MediumPadding3 = 40.dp

    val IndicatorSize = 14.dp

    val SmallIconSize = 11.dp
    val IconSize = 20.dp

    val ArticleCardSize = 96.dp

    val ArticleImageHeight = 248.dp


    val shimmerImageSize = 360.dp
    val shimmerImageHeight = 550.dp

}

internal fun Modifier.shimmerEffect(cornerRadius: CornerRadius = CornerRadius(x = 12f, y = 12f)) = composed {
    val transition = rememberInfiniteTransition(label = "shimmer effect")
    val alpha = transition.animateFloat(
        initialValue = 0.2f, targetValue = 0.9f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "transparency of the background color"
    ).value
    val color =  Color(0xFF414243).copy(alpha = alpha)
    drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = cornerRadius
        )
    }
}

internal fun convertHotMapsToDisplaySize(
    hotMaps: List<MapArea>,
    originalImageWidth: Float,
    originalImageHeight: Float,
    displayWidth: Float,
    displayHeight: Float
): List<MapArea> {

    if (originalImageWidth == 0f || originalImageHeight == 0f || displayWidth == 0f || displayHeight == 0f) {
        // Return empty list if any of the dimensions are zero
        return emptyList()
    }
    try {

        val scaleX = if (originalImageWidth != 0f) displayWidth / originalImageWidth else 1f
        val scaleY = if (originalImageHeight != 0f) displayHeight / originalImageHeight else 1f

        val logData = SaveLogs(SaveLogDetails(
            appDetails = "AOS:[Utils > convertHotMapsToDisplaySize()]  hotMaps received to method = $hotMaps"
        ))
        Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = false)

        val convertedHotMaps = hotMaps.map { mapArea ->

            val logData = SaveLogs(SaveLogDetails(
                appDetails = "AOS:[Utils > convertHotMapsToDisplaySize()]  {pageId: } hotMaps BEFORE convertion = $mapArea"
            ))
            Logger.i("${logData.value.appDetails}", saveLogs = logData, sendToDB = false)

            val logData1 = SaveLogs(SaveLogDetails(
                appDetails = "AOS:[[Utils > convertHotMapsToDisplaySize()]  {pageId: } hotMaps AFTER convertion = ${mapArea.copy(
                    x1 = mapArea.x1 * scaleX,
                    y1 = mapArea.y1 * scaleY,
                    x2 = mapArea.x2 * scaleX,
                    y2 = mapArea.y2 * scaleY
                )}"
            ))
            Logger.i("${logData1.value.appDetails}", saveLogs = logData1, sendToDB = false)

            mapArea.copy(
                x1 = mapArea.x1 * scaleX,
                y1 = mapArea.y1 * scaleY,
                x2 = mapArea.x2 * scaleX,
                y2 = mapArea.y2 * scaleY
            )

            // print before and after mapArea

            /* MapArea(
                 x1 = mapArea.x1 * scaleX,
                 y1 = mapArea.y1 * scaleY,
                 x2 = mapArea.x2 * scaleX,
                 y2 = mapArea.y2 * scaleY,
                 lineWidth = mapArea.lineWidth,
                 color = mapArea.color,
                 isSelected = mapArea.isSelected,
                 hasAttached = mapArea.hasAttached
             )*/

        }

        println(convertedHotMaps)
        return convertedHotMaps
    } catch (e: Exception) {
        println(e)
        return emptyList()
    }


}

internal fun getScreenWidth(context: Context): Int {
    val displayMetrics = DisplayMetrics()
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

@Composable
internal fun pixelsToDp(pixels: Float): Dp {
    val density = LocalDensity.current
    return with(density) { pixels.toDp() }
}


internal fun parseHtmlString(input: String): AnnotatedString {

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

internal fun formatEventDates(startDateString: String?, endDateString: String?): String {



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

internal fun getDayOfMonthSuffix(day: Int): String {
    return when (day) {
        1, 21, 31 -> "st"
        2, 22 -> "nd"
        3, 23 -> "rd"
        else -> "th"
    }
}