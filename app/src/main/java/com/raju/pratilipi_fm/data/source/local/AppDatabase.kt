package com.raju.pratilipi_fm.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.raju.pratilipi_fm.data.model.Song
import com.raju.pratilipi_fm.data.source.local.dao.SongDao

@Database(entities = [Song::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract val songDao: SongDao

    companion object {
        const val DB_NAME = "Pratilipi.db"
    }
}
