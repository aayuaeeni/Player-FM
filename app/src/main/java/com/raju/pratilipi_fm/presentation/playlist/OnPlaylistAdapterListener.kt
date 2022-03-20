package com.raju.pratilipi_fm.presentation.playlist

import com.raju.pratilipi_fm.data.model.Song

interface OnPlaylistAdapterListener {

    fun playSong(song: Song, songs: ArrayList<Song>)

    fun removeSongItem(song: Song)
}