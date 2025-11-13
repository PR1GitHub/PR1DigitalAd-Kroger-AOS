package com.purered.pr1digitaladclassic

/*-- CIRCULAR DETAILS from getAdDetails API --*/
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

/*-- AD PAGE DETAILS from getPageDetails API --*/
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

internal data class PageContent(
    val contentType: String,
    val eventPageContentId: String,
    val mapConfig: String
)

internal data class HotMaps(
    val height: Int,
    val width: Int,
    val maps: List<MapArea>
)

/*-- PARSING JSON STRING from getPageDetails API --*/
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

/*-- OFFER DETAILS --*/
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
    val appUrl: String?,
    val isCoupon: Boolean?,
    val isShoppable: Boolean?,

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


/*-- SAVE LOGS --*/
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




/*-- OTHER --*/
internal  data class BoxData(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)