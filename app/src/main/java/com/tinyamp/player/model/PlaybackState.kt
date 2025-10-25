package com.tinyamp.player.model

/**
 * Represents the current playback state
 */
enum class PlaybackState {
    IDLE,
    PLAYING,
    PAUSED,
    STOPPED,
    BUFFERING
}
