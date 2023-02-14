package edu.zut.erasmus_plus.retrofit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

//Nasa Astrology picture of the day
interface ApodService {
    companion object {
        const val BASE_URL_APOD_ITEM = "https://api.nasa.gov/"
        const val BASE_URL_APOD_IMAGE = "https://apod.nasa.gov/"
        const val API_KEY = ""
        fun getApodItem(): ApodService {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL_APOD_ITEM)
                .build()
            return retrofit.create();
        }
        fun getApodImage(): ApodService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_APOD_IMAGE)
                .build()
            return retrofit.create();
        }

    }
    @GET
    fun downloadImageUrl(@Url fileUrl: String): Call<ResponseBody>

    @GET("planetary/apod")
    fun getApod(@Query("api_key") api_key: String, @Query("date") date: String ): Call<AstronomyPictureDayEntity>

}
//https://api.nasa.gov/planetary/apod?api_key=  &date=2022-11-15