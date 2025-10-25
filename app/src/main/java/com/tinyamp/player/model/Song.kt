package com.tinyamp.player.model

import android.net.Uri

/**
 * Data class representing a song/audio file
 */
data class Song(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long, // in milliseconds
    val albumArtUri: Uri?,
    val path: String
) {
    /**
     * Get formatted duration string (MM:SS)
     */
    fun getFormattedDuration(): String {
        val seconds = duration / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    /**
     * Get display artist name or "Unknown Artist"
     */
    fun getDisplayArtist(): String {
        return artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
    }

    /**
     * Get display album name or "Unknown Album"
     */
    fun getDisplayAlbum(): String {
        return album?.takeIf { it.isNotBlank() } ?: "Unknown Album"
    }
}
