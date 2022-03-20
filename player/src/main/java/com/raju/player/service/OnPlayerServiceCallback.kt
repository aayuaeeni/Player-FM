package com.raju.player.service

import com.raju.player.model.ASong

interface OnPlayerServiceCallback {

    fun updateSongData(song: ASong)

    fun updateSongProgress(duration: Long, position: Long)

    fun setBufferingData(isBuffering: Boolean)

    fun setVisibilityData(isVisibility: Boolean)

    fun setPlayStatus(isPlay: Boolean)

    fun stopService()
}