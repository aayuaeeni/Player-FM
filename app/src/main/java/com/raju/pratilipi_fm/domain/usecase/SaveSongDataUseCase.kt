package com.raju.pratilipi_fm.domain.usecase

import com.raju.pratilipi_fm.data.model.Song
import com.raju.pratilipi_fm.domain.repository.PlaylistRepository

class SaveSongDataUseCase(private val playlistRepository: PlaylistRepository) {

    fun saveSongItem(song: Song) {
        playlistRepository.saveSongData(song)
    }
}