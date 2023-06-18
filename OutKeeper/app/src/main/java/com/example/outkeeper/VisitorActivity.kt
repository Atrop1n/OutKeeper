package com.example.outkeeper

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.HttpURLConnection
import java.net.URL


class VisitorActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.visitor)
        var date_captured = "unknown"
        runBlocking {
            launch(Dispatchers.IO) {
                val url =
                    URL("https://1ulwsnilg3.execute-api.eu-central-1.amazonaws.com/v2/s3/?key=my-haarcascades-84808/recent_visitor_detected.jpg")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "HEAD"
                conn.connect()
                date_captured = conn.getHeaderField("Last-Modified")
            }
        }
        val date_time_text = findViewById<TextView>(R.id.textView_time)
        date_time_text.text = ("Captured on " + date_captured)
        val imageView = findViewById<ImageView>(R.id.imageView2)
        Glide.with(this)
            .load("https://1ulwsnilg3.execute-api.eu-central-1.amazonaws.com/v1/s3/?key=my-haarcascades-84808/recent_visitor_detected.jpg")
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
            .skipMemoryCache(true) // Disable memory cache
            .into(imageView)


    }

    override fun onBackPressed() {
        this.finish()
    }
    fun go_in(view: View) {
        runBlocking {
            launch(Dispatchers.IO) {
                val url = URL("http://outkeeper.eu.ngrok.io/update?let_in=1")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val response = inputStream.bufferedReader().use { it.readText() }
                    println(response)
                } else {
                    println("GET request failed with response code $responseCode")
                }

                connection.disconnect()
            }}
        Toast.makeText(this, "You just let the person in", Toast.LENGTH_LONG).show();
    }

}

