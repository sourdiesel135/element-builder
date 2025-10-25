package com.tinyamp.player.data.repository

import android.content.ContentResolver
import com.tinyamp.player.data.models.Song
import com.tinyamp.player.utils.AudioScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicRepository(contentResolver: ContentResolver) {
    private val audioScanner = AudioScanner(contentResolver)

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: Flow<List<Song>> = _songs.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist: Flow<List<Song>> = _currentPlaylist.asStateFlow()

    suspend fun loadSongs() {
        val scannedSongs = audioScanner.scanAudioFiles()
        _songs.value = scannedSongs
        if (_currentPlaylist.value.isEmpty()) {
            _currentPlaylist.value = scannedSongs
        }
    }

    suspend fun getSongById(id: Long): Song? {
        return audioScanner.getSongById(id)
    }

    fun setCurrentPlaylist(songs: List<Song>) {
        _currentPlaylist.value = songs
    }

    fun getCurrentPlaylist(): List<Song> {
        return _currentPlaylist.value
    }

    fun searchSongs(query: String): List<Song> {
        if (query.isBlank()) return _songs.value

        val lowercaseQuery = query.lowercase()
        return _songs.value.filter {
            it.title.lowercase().contains(lowercaseQuery) ||
            it.artist.lowercase().contains(lowercaseQuery) ||
            it.album.lowercase().contains(lowercaseQuery)
        }
    }

    fun getSongsByArtist(artist: String): List<Song> {
        return _songs.value.filter { it.artist == artist }
    }

    fun getSongsByAlbum(album: String): List<Song> {
        return _songs.value.filter { it.album == album }
    }

    fun getArtists(): List<String> {
        return _songs.value.map { it.artist }.distinct().sorted()
    }

    fun getAlbums(): List<String> {
        return _songs.value.map { it.album }.distinct().sorted()
    }
}
