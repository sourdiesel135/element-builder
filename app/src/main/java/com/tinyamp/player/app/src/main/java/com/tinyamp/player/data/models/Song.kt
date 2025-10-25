package com.tinyamp.player.data.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtUri: Uri?,
    val size: Long,
    val dateAdded: Long,
    val mimeType: String
) : Parcelable {
    val displayDuration: String
        get() {
            val minutes = (duration / 1000) / 60
            val seconds = (duration / 1000) % 60
            return String.format("%d:%02d", minutes, seconds)
        }

    val displayArtist: String
        get() = if (artist.isNotEmpty() && artist != "<unknown>") artist else "Unknown Artist"

    val displayAlbum: String
        get() = if (album.isNotEmpty() && album != "<unknown>") album else "Unknown Album"
}
