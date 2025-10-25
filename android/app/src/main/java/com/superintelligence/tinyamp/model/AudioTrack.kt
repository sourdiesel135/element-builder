package com.superintelligence.tinyamp.model

/**
 * Audio Track Model
 * Represents a single audio track in the TinyAmp consciousness interface
 */
data class AudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val url: String,
    val albumArt: String? = null,
    val duration: Long = 0L,
    val archiveId: String? = null,  // Archive.org identifier
    val metadata: Map<String, String> = emptyMap()
)
