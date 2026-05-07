package com.parkable.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL
import java.net.URLEncoder

@Serializable
data class GeoPlace(
    @SerialName("display_name") val displayName: String,
    val lat: String,
    val lon: String
)

object GeocodingService {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun search(query: String): List<GeoPlace> = withContext(Dispatchers.IO) {
        if (query.length < 3) return@withContext emptyList()
        runCatching {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val conn = URL(
                "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=5&addressdetails=0"
            ).openConnection()
            conn.setRequestProperty("User-Agent", "ParkableApp/1.0")
            conn.connectTimeout = 5_000
            conn.readTimeout = 5_000
            val text = conn.getInputStream().bufferedReader().readText()
            json.decodeFromString<List<GeoPlace>>(text)
        }.getOrDefault(emptyList())
    }
}