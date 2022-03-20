package com.raju.pratilipi_fm.domain.repository

import com.raju.pratilipi_fm.data.model.Song


interface PlaylistRepository {

    fun saveSongData(song: Song):Long

    fun getSongs(): List<Song>?

    fun delete(song: Song)

}