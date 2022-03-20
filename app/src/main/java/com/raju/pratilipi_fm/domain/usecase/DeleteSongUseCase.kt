package com.raju.pratilipi_fm.domain.usecase

import com.raju.pratilipi_fm.data.model.Song
import com.raju.pratilipi_fm.domain.repository.PlaylistRepository

class DeleteSongUseCase(private val playlistRepository: PlaylistRepository) {


    fun deleteSongItem(song: Song) {
        playlistRepository.delete(song)
    }
}