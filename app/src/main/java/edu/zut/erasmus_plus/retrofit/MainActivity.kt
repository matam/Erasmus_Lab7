package edu.zut.erasmus_plus.retrofit

import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.ResponseBody
import org.w3c.dom.Text
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection

import java.net.URL
import java.net.URLConnection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {

    private var clickCounter: Long=0
    lateinit var button: Button
    lateinit var imageView: ImageView
    lateinit var pictureInfo : TextView
    lateinit var progress : ProgressBar


    var current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var formattedDate = current.format(formatter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.button)
        reformatDate(clickCounter++)
        button.setText("Get Image " + formattedDate)

        imageView = findViewById(R.id.imageView)
        progress = findViewById(R.id.progressBar)
        progress.visibility= View.GONE
        pictureInfo = findViewById(R.id.pictureInfo)
        getApodItem()
        button.setOnClickListener {
            if (isNetworkConnected()) {
                getApodItem()
            }
        }
        }

    private fun getApodItem(){
        val service = ApodService.getApodItem()
        val serviceRequest = service.getApod(ApodService.API_KEY,formattedDate)
        imageView.visibility=View.INVISIBLE
        progress.visibility= View.VISIBLE
        pictureInfo.text=""
        serviceRequest.enqueue(object : retrofit2.Callback<AstronomyPictureDayEntity> {
            override fun onResponse(
                call: retrofit2.Call<AstronomyPictureDayEntity>,
                response: retrofit2.Response<AstronomyPictureDayEntity>
            ) {
                val apod = response.body()

                apod?.let{
                    pictureInfo.text="Author: " + apod.copyright + ", Title: " + apod.title + ", Date: " +apod.date
                    getApodImage(apod.url.toString())
                    Log.i(MainActivity::class.simpleName,"URL: " + apod.url)
                }
            }
            override fun onFailure(call: retrofit2.Call<AstronomyPictureDayEntity>, t: Throwable) {
                Log.i(MainActivity::class.simpleName, "on FAILURE!!!!")
            }
        })
    }

    private fun getApodImage(url: String){
        val service = ApodService.getApodImage()
        val serviceRequest = service.downloadImageUrl(url)
        serviceRequest.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                response.body()?.let{readStream(it.byteStream())
                    reformatDate(clickCounter++)
                    button.setText("Get Image " + formattedDate)
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                Log.i(MainActivity::class.simpleName, "on FAILURE!!!!")
            }

        })
    }

    private fun reformatDate(valueDays: Long)
    {
        current = LocalDateTime.now().minusDays(valueDays)
        formattedDate = current.format(formatter)

    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null &&  networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun downloadFile(inputUrl:String){
        val url = URL(inputUrl)
        val httpClient = url.openConnection() as HttpURLConnection
        httpClient.doInput=true
        httpClient.connectTimeout = 5000
        httpClient.readTimeout = 5000
        //httpClient.connect()
        if (httpClient.responseCode == HttpURLConnection.HTTP_OK) {
            try {
                val stream = BufferedInputStream(httpClient.inputStream)
                readStream(stream)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                httpClient.disconnect()
            }
        } else {
            Log.v("Error in Comuniaction","ERROR"+ httpClient.responseCode)
        }
    }

    private fun readStream(inputStream: InputStream) {
        val bitmapImage = BitmapFactory.decodeStream(inputStream)

        CoroutineScope(Dispatchers.Main).launch() {
            imageView.setImageBitmap(bitmapImage)
            imageView.visibility=View.VISIBLE
            progress.visibility= View.INVISIBLE
        }
    }

}



