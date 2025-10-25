package com.superintelligence.tinyamp.archiveorg

import com.superintelligence.tinyamp.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

/**
 * Archive.org Integration Client
 * Provides streaming and search capabilities from the Internet Archive
 */
class ArchiveOrgClient {

    companion object {
        private const val BASE_URL = "https://archive.org"
        private const val SEARCH_API = "$BASE_URL/advancedsearch.php"
        private const val METADATA_API = "$BASE_URL/metadata"
    }

    /**
     * Search Archive.org for audio content
     */
    suspend fun searchAudio(
        query: String,
        maxResults: Int = 20,
        mediaType: String = "audio"
    ): List<AudioTrack> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "$SEARCH_API?" +
                    "q=$encodedQuery" +
                    "&fl[]=identifier,title,creator,format" +
                    "&mediatype=$mediaType" +
                    "&rows=$maxResults" +
                    "&output=json"

            val response = URL(searchUrl).readText()
            val json = JSONObject(response)
            val docs = json.getJSONObject("response").getJSONArray("docs")

            val tracks = mutableListOf<AudioTrack>()

            for (i in 0 until docs.length()) {
                val doc = docs.getJSONObject(i)
                val identifier = doc.optString("identifier", "")
                val title = doc.optString("title", "Unknown")
                val creator = doc.optString("creator", "Unknown Artist")

                if (identifier.isNotEmpty()) {
                    // Get direct streaming URL
                    val streamUrl = getStreamingUrl(identifier)

                    tracks.add(
                        AudioTrack(
                            id = identifier,
                            title = title,
                            artist = creator,
                            url = streamUrl,
                            archiveId = identifier,
                            albumArt = "$BASE_URL/services/img/$identifier"
                        )
                    )
                }
            }

            tracks
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get metadata for a specific Archive.org item
     */
    suspend fun getMetadata(identifier: String): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            val metadataUrl = "$METADATA_API/$identifier"
            val response = URL(metadataUrl).readText()
            val json = JSONObject(response)
            val metadata = json.optJSONObject("metadata") ?: return@withContext emptyMap()

            mapOf(
                "title" to metadata.optString("title", "Unknown"),
                "creator" to metadata.optString("creator", "Unknown Artist"),
                "date" to metadata.optString("date", ""),
                "description" to metadata.optString("description", ""),
                "subject" to metadata.optString("subject", "")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    /**
     * Get direct streaming URL for an Archive.org audio item
     */
    private suspend fun getStreamingUrl(identifier: String): String = withContext(Dispatchers.IO) {
        try {
            // Get file list from metadata
            val metadataUrl = "$METADATA_API/$identifier"
            val response = URL(metadataUrl).readText()
            val json = JSONObject(response)
            val files = json.optJSONArray("files")

            if (files != null) {
                // Look for MP3 or other audio formats
                for (i in 0 until files.length()) {
                    val file = files.getJSONObject(i)
                    val name = file.optString("name", "")
                    val format = file.optString("format", "")

                    if (format.contains("mp3", ignoreCase = true) ||
                        format.contains("vorbis", ignoreCase = true) ||
                        name.endsWith(".mp3", ignoreCase = true)
                    ) {
                        return@withContext "$BASE_URL/download/$identifier/$name"
                    }
                }
            }

            // Fallback: construct a generic streaming URL
            "$BASE_URL/download/$identifier"
        } catch (e: Exception) {
            e.printStackTrace()
            "$BASE_URL/download/$identifier"
        }
    }

    /**
     * Get consciousness-themed audio collections
     */
    suspend fun getConsciousnessAudio(): List<AudioTrack> {
        val queries = listOf(
            "ambient consciousness meditation",
            "binaural beats theta",
            "neural synchronization frequency",
            "dimensional ambient music"
        )

        val allTracks = mutableListOf<AudioTrack>()

        queries.forEach { query ->
            val tracks = searchAudio(query, maxResults = 5)
            allTracks.addAll(tracks)
        }

        return allTracks.distinctBy { it.id }
    }

    /**
     * Search for specific frequency-based audio
     */
    suspend fun searchByFrequency(frequency: String): List<AudioTrack> {
        return searchAudio("$frequency hz binaural solfeggio frequency")
    }

    /**
     * Get archive.org live radio streams
     */
    fun getLiveStreams(): List<AudioTrack> {
        return listOf(
            AudioTrack(
                id = "live-music-archive",
                title = "Live Music Archive Stream",
                artist = "Archive.org",
                url = "https://archive.org/download/etree",
                archiveId = "etree"
            ),
            AudioTrack(
                id = "oldtime-radio",
                title = "Old Time Radio",
                artist = "Archive.org Radio",
                url = "https://archive.org/download/oldtimeradio",
                archiveId = "oldtimeradio"
            )
        )
    }
}
