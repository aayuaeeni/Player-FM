package com.raju.pratilipi_fm.data.repository

import com.raju.pratilipi_fm.data.model.Song
import com.raju.pratilipi_fm.data.source.local.AppDatabase
import com.raju.pratilipi_fm.domain.repository.PlaylistRepository

class PlaylistRepositoryImp(private val appDatabase: AppDatabase) : PlaylistRepository {

    override fun delete(song: Song) {
        appDatabase.songDao.delete(song)
    }

    override fun getSongs(): List<Song>? {
        return appDatabase.songDao.loadAll()
    }

    override fun saveSongData(song: Song):Long {
        return appDatabase.songDao.insert(song)
    }
}