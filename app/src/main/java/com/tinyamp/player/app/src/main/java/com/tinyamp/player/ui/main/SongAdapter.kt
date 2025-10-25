package com.tinyamp.player.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tinyamp.player.R
import com.tinyamp.player.data.models.Song
import com.tinyamp.player.databinding.ItemSongBinding

class SongAdapter(
    private val onSongClick: (Song, Int) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    private var currentPlayingSongId: Long? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song, song.id == currentPlayingSongId)
    }

    fun setCurrentPlayingSong(songId: Long?) {
        val previousId = currentPlayingSongId
        currentPlayingSongId = songId

        // Update the previous and current items
        currentList.indexOfFirst { it.id == previousId }.let { index ->
            if (index != -1) notifyItemChanged(index)
        }
        currentList.indexOfFirst { it.id == songId }.let { index ->
            if (index != -1) notifyItemChanged(index)
        }
    }

    inner class SongViewHolder(
        private val binding: ItemSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSongClick(getItem(position), position)
                }
            }
        }

        fun bind(song: Song, isPlaying: Boolean) {
            binding.apply {
                textSongTitle.text = song.title
                textSongArtist.text = song.displayArtist
                textSongDuration.text = song.displayDuration

                // Load album art
                Glide.with(root.context)
                    .load(song.albumArtUri)
                    .placeholder(R.drawable.ic_music_note)
                    .error(R.drawable.ic_music_note)
                    .centerCrop()
                    .into(imageAlbumArt)

                // Highlight currently playing song
                root.alpha = if (isPlaying) 1.0f else 0.7f
                root.isActivated = isPlaying
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
