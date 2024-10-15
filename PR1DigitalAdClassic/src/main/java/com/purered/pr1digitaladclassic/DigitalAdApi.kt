package com.purered.pr1digitaladclassic

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

internal class DigitalAdApi {
}

enum class ApiEnv {
    PROD,
    QA
}



// Define an Interceptor to add the X-Api-Key header
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("XApiKey", apiKey)
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
//bqwwosbzrzcvffztxzyczieljzsahmkp
//https://oms-kroger-webapp-da-classic-api-prod.przone.net

internal fun getBaseUrl(environment: ApiEnv): String {
    return when (environment) {
        ApiEnv.PROD -> "https://oms-kroger-webapp-da-classic-api-prod.przone.net/api/"
        ApiEnv.QA ->   "https://oms-kroger-webapp-da-classic-api-qa.przone.net/api/"
    }
}

internal fun createRetrofit(environment: ApiEnv, apiKey: String): Retrofit {
    val interceptor = ApiKeyInterceptor(apiKey)
    val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    return Retrofit.Builder()
        .baseUrl(getBaseUrl(environment))
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

//internal fun createRetrofit(environment: Environment): Retrofit {
//    return Retrofit.Builder()
//        .baseUrl(getBaseUrl(environment))
//        .addConverterFactory(GsonConverterFactory.create())
//
//        .build()
//}

internal fun createWeeklyAdService(environment: ApiEnv,apiKey: String): ApiService {
    val retrofit = createRetrofit(environment,apiKey)
    return retrofit.create(ApiService::class.java)
}


internal  var weeklyAdService = createWeeklyAdService(ApiEnv.QA,"pgH7QzFHJx4w46fI~5Uzi4RvtTwlEXp2");






//private  val  retrofit = Retrofit.Builder().baseUrl("https://oms-kroger-webapp-da-classic-api-qa.azurewebsites.net/api/")
//    .addConverterFactory(GsonConverterFactory.create())
//    .build()
//
//internal var weeklyAdService = retrofit.create((ApiService::class.java))

internal interface ApiService{

    @GET("events/{eventId}/dac")
    suspend fun getEventDetails(
        @Path("eventId") eventId: Int,
        @Query("mode") mode: String
    ): WeeklyAd

    //https://oms-kroger-webapp-da-classic-api-qa.azurewebsites.net/api/dacs/241a4bd1-9b92-42ef-9ec0-18169bbe83eb?location=03500590
    @GET("dacs/{adId}")
    suspend fun getAdDetails(
        @Path("adId") eventId: String,
        @Query("location") location: String
    ): WeeklyAd

    //https://localhost:44341/api/dacs/{adId}/pages/{pageId}?location=
//{location}
    //dacs/241a4bd1-9b92-42ef-9ec0-18169bbe83eb/pages/09893878-883b-4044-a054-28375a0a0e6c?location=03500590
    @GET("dacs/{adId}/pages/{pageId}")
    suspend fun getPageDetails(
        @Path("adId") adId: String,
        @Path("pageId") pageId: String,
        @Query("location") location: String
    ): AdPage


    ///dacs/{adId}/offers/{offerId}?location=
//{location}
    @GET("dacs/{adId}/offers/{offerId}")
    suspend fun getOfferDetails(
        @Path("adId") adId: String,
        @Path("offerId") offerId: String,
        @Query("location") location: String
    ):OfferDetails




}