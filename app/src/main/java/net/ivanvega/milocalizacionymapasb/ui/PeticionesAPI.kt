package net.ivanvega.milocalizacionymapasb.ui

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.ArrayList

fun decodePoly(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].toInt() - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].toInt() - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val latLng = LatLng((lat.toDouble() / 1E5),
            (lng.toDouble() / 1E5))
        poly.add(latLng)
    }

    return poly
}

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

            val jsonString = response.body?.string()
            println(jsonString)

            val jsonObject = JsonParser().parse(jsonString).asJsonObject

            val routes = jsonObject.getAsJsonArray("routes")
            val legs = routes[0].asJsonObject.getAsJsonArray("legs")
            val steps = legs[0].asJsonObject.getAsJsonArray("steps")

            val latLngs = ArrayList<LatLng>()

            for (step in steps) {
                val polyline = step.asJsonObject.getAsJsonObject("polyline").get("points").asString
                latLngs.addAll(decodePoly(polyline))
            }

            println(latLngs)
        }
    }
}
