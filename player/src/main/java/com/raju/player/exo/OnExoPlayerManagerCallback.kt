package com.raju.player.exo

import com.raju.player.model.ASong
import java.util.ArrayList

interface OnExoPlayerManagerCallback {

    fun getCurrentStreamPosition(): Long

    fun stop()

    fun play(aSong: ASong)

    fun pause()

    fun seekTo(position: Long)

    fun setCallback(callback: OnSongStateCallback)

    /**
     * This class gives the information about current song
     * (position, the state of completion, when it`s changed, ...)
     *
     * */
    interface OnSongStateCallback {

        fun onCompletion()

        fun onPlaybackStatusChanged(state : Int)

        fun setCurrentPosition(position: Long, duration: Long)

        fun getCurrentSong(): ASong?

        fun getCurrentSongList(): ArrayList<ASong>?

        fun shuffle(isShuffle: Boolean)

        fun repeat(isRepeat: Boolean)

        fun repeatAll(isRepeatAll: Boolean)

    }

}
