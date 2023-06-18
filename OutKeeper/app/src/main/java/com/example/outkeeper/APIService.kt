package com.example.outkeeper

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.concurrent.TimeUnit


interface APIService {
    @POST("{api_endpoint}")
    suspend fun createPhoto(@Path("api_endpoint") api_endpoint: String): Response<ResponseBody>
    @HEAD("{api_endpoint}")
    suspend fun getPhotoMetadata(@Path("api_endpoint") api_endpoint: String): Void
    @PUT("/v1/my-facerecognizerbucket-84808/{photo_name}")
    suspend fun uploadPhoto(@Path("photo_name") employeeId: String, @Body requestBody: RequestBody): Response<ResponseBody>
    @GET("v1/my-facerecognizerbucket-84808/")
    suspend fun getPhoto(): Response<ResponseBody>
    @DELETE("v1/my-facerecognizerbucket-84808/{photo_name}")
    suspend fun deletePhoto(@Path("photo_name") employeeId: String) :Response<ResponseBody>
}
suspend fun headMethod(api_endpoint: String) {
    // Create Retrofit
    val response_data = getHeadResponse(api_endpoint)
    // Handle the response data here
    //return response_data
}
suspend fun postMethod(api_endpoint: String): String {
    // Create Retrofit
    val response_data = getPostResponse(api_endpoint)
    // Handle the response data here
    return response_data
}
suspend fun putMethod(file: File, photo_name:String): String {
    val response_data = getPutResponse(file,photo_name)
    // Handle the response data here
    return response_data
}

@SuppressLint("SuspiciousIndentation")
suspend fun getPutResponse(file: File, photo_name:String): String {
    // Create Retrofit
    val retrofit = Retrofit.Builder()
        .baseUrl("https://m1gm2lloge.execute-api.eu-central-1.amazonaws.com/")
        .build()

    // Create Service
    val service = retrofit.create(APIService::class.java)

    val imageRequestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        println(imageRequestBody.contentType())
        // Do the PUT request and get response
        val response = service.uploadPhoto(photo_name,imageRequestBody)


            if (response.isSuccessful) {

                // Convert raw JSON to pretty JSON using GSON library
                val gson = GsonBuilder().setPrettyPrinting().create()
                val prettyJson = gson.toJson(
                    JsonParser.parseString(
                        response.body()
                            ?.string() // About this thread blocking annotation : https://github.com/square/retrofit/issues/3255
                    )
                )
                return response.body()?.string().toString()

            } else {
                throw Exception("Retrofit error: ${response.code()}")
            }
}
suspend fun getHeadResponse(api_endpoint: String) {
    val retrofit = Retrofit.Builder()
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .baseUrl("https://1ulwsnilg3.execute-api.eu-central-1.amazonaws.com/v2/")
        .build()

    val service = retrofit.create(APIService::class.java)

    val response = service.getPhotoMetadata(api_endpoint)

    println(response)
}
suspend fun getPostResponse(api_endpoint: String): String {
    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .build()
    val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .baseUrl("https://xuhqq2omx3.execute-api.eu-central-1.amazonaws.com/v1/")
        .build()


    val service = retrofit.create(APIService::class.java)

    val response = service.createPhoto(api_endpoint)

    if (response.isSuccessful) {
        return response.body()?.string().toString()
    } else {
        throw Exception("Retrofit error: ${response.code()}")
    }
}
suspend fun getResponseData(): String {
    val retrofit = Retrofit.Builder()
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .baseUrl("https://rxv9mixnlg.execute-api.eu-central-1.amazonaws.com/")
        .build()

    val service = retrofit.create(APIService::class.java)

    val response = service.getPhoto()

    if (response.isSuccessful) {
        return response.body()?.string().toString()
    } else {
        throw Exception("Retrofit error: ${response.code()}")
    }
}

suspend fun getMethod():String  {

    val response_data = getResponseData()
    // Handle the response data here
    return response_data
}

fun deleteMethod(photo_name: String) {

    // Create Retrofit
    val retrofit = Retrofit.Builder()
        .baseUrl("https://5da1exhj2c.execute-api.eu-central-1.amazonaws.com/")
        .build()

    // Create Service
    val service = retrofit.create(APIService::class.java)

    CoroutineScope(Dispatchers.IO).launch {

        // Do the DELETE request and get response

        val response = service.deletePhoto(photo_name)
        withContext(Dispatchers.Main) {
            if (response.isSuccessful) {

                // Convert raw JSON to pretty JSON using GSON library
                val gson = GsonBuilder().setPrettyPrinting().create()
                val prettyJson = gson.toJson(
                    JsonParser.parseString(
                        response.body()
                            ?.string() // About this thread blocking annotation : https://github.com/square/retrofit/issues/3255
                    )
                )

                Log.d("Pretty Printed JSON :", prettyJson)

            } else {

                Log.e("RETROFIT_ERROR", response.code().toString())

            }
        }
    }
}