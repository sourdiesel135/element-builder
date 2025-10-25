package com.tinyamp.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinyamp.player.model.Song
import com.tinyamp.player.service.PlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var playbackService: PlaybackService? = null

    fun setPlaybackService(service: PlaybackService) {
        playbackService = service

        // Observe service state
        viewModelScope.launch {
            service.currentSong.collect { song ->
                _currentSong.value = song
            }
        }

        viewModelScope.launch {
            service.isPlaying.collect { playing ->
                _isPlaying.value = playing
            }
        }
    }

    fun setSongs(songList: List<Song>) {
        _songs.value = songList
    }

    fun playSong(song: Song) {
        val index = _songs.value.indexOf(song)
        if (index >= 0) {
            playbackService?.setPlaylist(_songs.value, index)
        }
    }

    fun playPause() {
        playbackService?.playPause()
    }

    fun playNext() {
        playbackService?.playNext()
    }

    fun playPrevious() {
        playbackService?.playPrevious()
    }

    fun seekTo(position: Long) {
        playbackService?.seekTo(position)
    }

    fun getCurrentPosition(): Long {
        return playbackService?.getCurrentPosition() ?: 0L
    }

    fun getDuration(): Long {
        return playbackService?.getDuration() ?: 0L
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
}
