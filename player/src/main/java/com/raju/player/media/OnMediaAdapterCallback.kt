package com.raju.player.media


import com.raju.player.model.ASong
import java.util.ArrayList

interface OnMediaAdapterCallback {

    fun onSongChanged(song : ASong)

    fun onPlaybackStateChanged(state : Int)

    fun setDuration(duration: Long, position: Long)

    fun addNewPlaylistToCurrent(songList: ArrayList<ASong>)

    fun onShuffle(isShuffle: Boolean)

    fun onRepeat(isRepeat: Boolean)

    fun onRepeatAll(repeatAll: Boolean)

}