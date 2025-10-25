package com.tinyamp.player.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tinyamp.player.R
import com.tinyamp.player.model.Song

class SongAdapter(
    private val onSongClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    private var currentlyPlayingSongId: Long? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view, onSongClick)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song, song.id == currentlyPlayingSongId)
    }

    fun setCurrentlyPlaying(songId: Long?) {
        val oldPlayingPosition = currentList.indexOfFirst { it.id == currentlyPlayingSongId }
        val newPlayingPosition = currentList.indexOfFirst { it.id == songId }

        currentlyPlayingSongId = songId

        if (oldPlayingPosition >= 0) {
            notifyItemChanged(oldPlayingPosition)
        }
        if (newPlayingPosition >= 0) {
            notifyItemChanged(newPlayingPosition)
        }
    }

    class SongViewHolder(
        itemView: View,
        private val onSongClick: (Song) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val titleTextView: TextView = itemView.findViewById(R.id.songTitle)
        private val artistTextView: TextView = itemView.findViewById(R.id.songArtist)
        private val durationTextView: TextView = itemView.findViewById(R.id.songDuration)
        private val playingIndicator: ImageView = itemView.findViewById(R.id.playingIndicator)

        fun bind(song: Song, isCurrentlyPlaying: Boolean) {
            titleTextView.text = song.title
            artistTextView.text = "${song.getDisplayArtist()} • ${song.getDisplayAlbum()}"
            durationTextView.text = song.getFormattedDuration()

            playingIndicator.visibility = if (isCurrentlyPlaying) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onSongClick(song)
            }
        }
    }

    private class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}
