package com.purered.pr1addemo.data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class PageContent(
    val contentType: String,
    val eventPageContentId: String,
    val mapConfig: String
)


data class AdPage(
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


data class OfferDetails(
    val eventId: Int,
    val eventName: String,
    val startDate: String,
    val endDate: String,
    val headline: String,
    val bodyCopy: String,
    val imageURL: String,
    val pricingTemplateName: String,
    val mathConfig: String,
    val montageImageURL: String,
    val withCard: String?,
    val wic: String,
    val offerDisclaimer: String,
    val eventDisclaimer: String?,
    val itemToPicture: String,
    val eightPartKey: String?,
    val isBaseOffer: Boolean,
    val offerVersionId: Int,
    val offerVersionGroupId: Int,
    val offerCategoryName: String?,
    val genericDisclaimer:String?
)

data class WeeklyAd(
    val adDescription: String,
    val adTitle: String,
    val circularType: String,
    val endDate: String,
    val eventId: Int,
    val eventName: String,
    val pages: List<AdPage>,
)

data class WeeklyAdResponse(
    val weeklyAd: WeeklyAd
)


private  val  retrofit = Retrofit.Builder().baseUrl("https://oms-kroger-webapp-da-classic-api-qa.azurewebsites.net/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

var weeklyAdService = retrofit.create((ApiService::class.java))

interface ApiService{


    @GET("events/{eventId}/dac")
    suspend fun getEventDetails(
        @Path("eventId") eventId: Int,
        @Query("mode") mode: String
    ): WeeklyAd



    @GET("events/{eventId}/dac/pages/{pageId}")
    suspend fun getPageDetails(
        @Path("eventId") eventId: Int,
        @Path("pageId") pageId: String,
        @Query("mode") mode: String
    ): AdPage



    @GET("events/11796/dac/offerDetails?offerVersionProductGroupId=256519")
    suspend fun getOfferDetailsStatic( ):List<OfferDetails>

    @GET("events/{eventId}/dac/offerDetails")
    suspend fun getOfferDetails(
        @Path("eventId") eventId: Int,
        @Query("offerVersionProductGroupId") offerVersionProductGroupId: Int
    ):List<OfferDetails>




}