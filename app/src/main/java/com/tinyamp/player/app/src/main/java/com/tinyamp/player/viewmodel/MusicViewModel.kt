package com.tinyamp.player.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tinyamp.player.data.models.PlaybackState
import com.tinyamp.player.data.models.PlaybackStatus
import com.tinyamp.player.data.models.RepeatMode
import com.tinyamp.player.data.models.Song
import com.tinyamp.player.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository(application.contentResolver)

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadSongs()
    }

    fun loadSongs() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.loadSongs()
                repository.songs.collect { songList ->
                    _songs.value = songList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading songs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePlaybackState(state: PlaybackState) {
        _playbackState.value = state
    }

    fun updatePlaybackPosition(position: Long) {
        _playbackState.value = _playbackState.value.copy(currentPosition = position)
    }

    fun toggleShuffle() {
        val newShuffleState = !_playbackState.value.isShuffleEnabled
        _playbackState.value = _playbackState.value.copy(isShuffleEnabled = newShuffleState)
    }

    fun toggleRepeatMode() {
        val newRepeatMode = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playbackState.value = _playbackState.value.copy(repeatMode = newRepeatMode)
    }

    fun searchSongs(query: String) {
        viewModelScope.launch {
            _songs.value = repository.searchSongs(query)
        }
    }

    fun getCurrentPlaylist(): List<Song> {
        return repository.getCurrentPlaylist()
    }

    fun setCurrentPlaylist(songs: List<Song>) {
        repository.setCurrentPlaylist(songs)
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
