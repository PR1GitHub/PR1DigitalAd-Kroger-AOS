package com.purered.pr1digitaladclassic

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
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
import java.text.SimpleDateFormat
import java.util.Locale

internal data class PageContent(
    val contentType: String,
    val eventPageContentId: String,
    val mapConfig: String
)




internal data class AdPage(
    val eventPageId: String,
    val page: String,
    val version: String,
    val fileName: String,
    val fileURL: String,
    val compressedFileURL: String,
    val fileStatus: String,
    val status: String,
    val stores: String,
    val config: Any?,
    val fileUploadedDate: Any?,
    val startDate: Any?,
    val endDate: Any?,
    val pageSetting: Any?,
    val contents: List<PageContent>
)


internal data class OfferDetails(

    // By Default All are optional atrributes, should be validated when converting to SpotClickPayload,

    // Attributes will be exposed to Client
    val id: String?,
    val headline: String?,
    val bodyCopy: String?,
    val imageURL: String?,
    val pricingHTML: String?,
    val pricingText: String?,
    val upc: String?,
    val startDate: String?,
    val endDate: String?,
    val category: String?,
    val disclaimer: String?,
    val webURL: String?,
    val appURL: String?,

    // Extra attributes coming from API
    val eventId: Int?,
    val eventName: String?,
    val pricingTemplateName: String?,
    val mathConfig: String?,
    val montageImageURL: String?,
    val withCard: String?,
    val wic: String?,
    val offerDisclaimer: String?,
    val eventDisclaimer: String?,
    val itemToPicture: String?,
    val eightPartKey: String?,
    val isBaseOffer: Boolean?,
    val offerVersionId: Int?,
    val offerVersionGroupId: Int?,
    val offerCategoryName: String?,
    val genericDisclaimer:String?
)

internal data class WeeklyAd(
    val adDescription: String,
    val adTitle: String,
    val circularType: String,
    val endDate: String,
    val eventId: Int,
    val eventName: String,
    val pages: List<AdPage>,
    val logLevelCode: Int,
    val isLogEnabled: Boolean
)



internal data class HotMaps(
    val height: Int,
    val width: Int,
    val maps: List<MapArea>
)

internal data class MapAreaContent(
    val altText: String,
    val appUrl: String,
    val event: String,
    val id: String,
    val webUrl: String,
    val bodyCopy: String,
    val headline: String,
    val imageURL: String,
    val index: String,
    val offerVersionProductGroupId: String,
)


internal data class MapArea(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val lineWidth: Int,
    val color: String,
    val isSelected: Boolean,
    val hasAttached: Boolean,
    val contentId: String? = null,
    val contentType: String? = null, //"Creative" or "Offer"
    val eventPageContentId: String? = null,
    var content: MapAreaContent? = null

)


internal  data class BoxData(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)


data class SaveLogs(
    val value: SaveLogDetails
)

data class SaveLogDetails(
    val adId: String? = "",
    val loc: String? = "",
    val offerId: String? = "",
    val url: String? = "",
    val appDetails: String? = ""
)