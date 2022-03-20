package com.raju.pratilipi_fm.domain.usecase

import com.raju.pratilipi_fm.data.model.Song
import com.raju.pratilipi_fm.domain.repository.PlaylistRepository

class GetSongsUseCase(private val playlistRepository: PlaylistRepository) {
    fun getSongs(): List<Song>? {
        return playlistRepository.getSongs()
    }
}