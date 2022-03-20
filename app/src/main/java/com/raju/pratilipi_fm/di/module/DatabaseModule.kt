package com.raju.pratilipi_fm.di.module

import android.app.Application
import androidx.room.Room
import com.raju.pratilipi_fm.data.repository.PlaylistRepositoryImp
import com.raju.pratilipi_fm.data.source.local.AppDatabase
import com.raju.pratilipi_fm.data.source.local.dao.SongDao
import com.raju.pratilipi_fm.domain.repository.PlaylistRepository
import org.koin.dsl.module

val DatabaseModule = module {

    single { createAppDatabase(get()) }

    single { createSongDao(get()) }

}

internal fun createAppDatabase(application: Application): AppDatabase {
    return Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        AppDatabase.DB_NAME
    )
         .fallbackToDestructiveMigration()//allows database to be cleared after upgrading version
        .allowMainThreadQueries()
        .build()
}


fun createSongDao(appDatabase: AppDatabase): SongDao {
    return appDatabase.songDao
}


fun createPlaylistRepository(appDatabase: AppDatabase): PlaylistRepository {
    return PlaylistRepositoryImp(appDatabase)
}

