package net.ivanvega.milocalizacionymapasb.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

fun peticionAPIDirections() {
    GlobalScope.launch(Dispatchers.IO) {

        val client = OkHttpClient()
        val origin = "origin=uriangato"
        val destination = "destination=moroleon"
        val apiKey = "key=AIzaSyAhLjXhdCXWEFzgTlgytVfvYXB6FR6Htxg"

        val request = Request.Builder()
            .url("https://maps.googleapis.com/maps/api/directions/json?$origin&$destination&$apiKey")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            println(response.body.string())
        }
    }
}
