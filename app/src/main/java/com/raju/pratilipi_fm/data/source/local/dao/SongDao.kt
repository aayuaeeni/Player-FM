package com.raju.pratilipi_fm.data.source.local.dao

import androidx.room.*
import com.raju.pratilipi_fm.data.model.Song


@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(song: Song): Long

    @Query("SELECT * FROM Song")
    fun loadAll(): MutableList<Song>

    @Delete
    fun delete(song: Song)

    @Query("DELETE FROM Song")
    fun deleteAll()

    @Query("SELECT * FROM Song where id = :songId")
    fun loadOneBySongId(songId: Long): Song?

    @Query("SELECT * FROM Song where title = :songTitle")
    fun loadOneBySongTitle(songTitle: String): Song?

    @Update
    fun update(song: Song)

}