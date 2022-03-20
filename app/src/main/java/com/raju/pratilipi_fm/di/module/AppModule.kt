package com.raju.pratilipi_fm.di.module

import com.raju.pratilipi_fm.domain.repository.PlaylistRepository
import com.raju.pratilipi_fm.domain.usecase.DeleteSongUseCase
import com.raju.pratilipi_fm.domain.usecase.GetSongsUseCase
import com.raju.pratilipi_fm.domain.usecase.SaveSongDataUseCase
import com.raju.pratilipi_fm.presentation.playlist.PlaylistViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val AppModule = module {

    viewModel { PlaylistViewModel(get(), get(), get()) }

    single { createGetSongsUseCase(get()) }

    single { createDeleteSongUseCase(get()) }

    single { createSaveSongDataUseCase(get()) }

    single { createPlaylistRepository(get()) }
}


fun createSaveSongDataUseCase(
    playlistRepository: PlaylistRepository
): SaveSongDataUseCase {
    return SaveSongDataUseCase(playlistRepository)
}

fun createDeleteSongUseCase(
    playlistRepository: PlaylistRepository
): DeleteSongUseCase {
    return DeleteSongUseCase(playlistRepository)
}


fun createGetSongsUseCase(
    playlistRepository: PlaylistRepository
): GetSongsUseCase {
    return GetSongsUseCase(playlistRepository)
}
