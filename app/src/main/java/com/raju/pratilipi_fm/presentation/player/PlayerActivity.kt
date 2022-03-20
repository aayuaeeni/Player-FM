package com.raju.pratilipi_fm.presentation.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.lifecycle.Observer
import coil.load
import coil.request.CachePolicy
import com.raju.player.BasePlayerActivity
import com.raju.player.model.ASong
import com.raju.player.util.OnSwipeTouchListener
import com.raju.player.util.formatTimeInMillisToString
import com.raju.pratilipi_fm.R
import com.raju.pratilipi_fm.data.model.Song
import com.raju.pratilipi_fm.databinding.ActivityPlayerBinding
import java.io.File

class PlayerActivity : BasePlayerActivity() {

    lateinit var binding: ActivityPlayerBinding

    private var mSong: Song? = null
    private var mSongList: MutableList<ASong>? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.extras?.apply {
            if (containsKey(SONG_LIST_KEY)) {
                mSongList = getParcelableArrayList(SONG_LIST_KEY)
            }

            if (containsKey(ASong::class.java.name)) {
                mSong = getParcelable<ASong>(ASong::class.java.name) as Song
                mSong?.let {
                    mSongList?.let { it1 -> play(it1, it) }
                    loadInitialData(it)
                }
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onNewIntent(intent)

        with(songPlayerViewModel) {

            songDurationData.observe(this@PlayerActivity) {
                binding.songPlayerProgressSeekBar.max = it
            }

            songPositionTextData.observe(this@PlayerActivity,
                Observer { t -> binding.songPlayerPassedTimeTextView.text = t })

            songPositionData.observe(this@PlayerActivity) {
                binding.songPlayerProgressSeekBar.progress = it
            }

            isRepeatData.observe(this@PlayerActivity) {
                binding.songPlayerRepeatImageView.setImageResource(
                    if (it) R.drawable.ic_repeat_one_color_primary_vector
                    else R.drawable.ic_repeat_one_black_vector
                )
            }

            isShuffleData.observe(this@PlayerActivity) {
                binding.songPlayerShuffleImageView.setImageResource(
                    if (it) R.drawable.ic_shuffle_color_primary_vector
                    else R.drawable.ic_shuffle_black_vector
                )
            }

            isPlayData.observe(this@PlayerActivity) {
                binding.songPlayerToggleImageView.setImageResource(if (it) R.drawable.ic_pause_vector else R.drawable.ic_play_vector)
            }

            playerData.observe(this@PlayerActivity) {
                loadInitialData(it)
            }
        }

        binding.songPlayerContainer.setOnTouchListener(object :
            OnSwipeTouchListener(this@PlayerActivity) {
            override fun onSwipeRight() {
                if (mSongList?.size ?: 0 > 1) previous()

            }

            override fun onSwipeLeft() {
                if (mSongList?.size ?: 0 > 1) next()
            }
        })

        binding.songPlayerProgressSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) seekTo(p1.toLong())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                //Nothing to do here
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                //Nothing to do here
            }

        })

        binding.songPlayerSkipNextImageView.setOnClickListener {
            next()
        }

        binding.songPlayerSkipBackImageView.setOnClickListener {
            previous()
        }

        binding.songPlayerToggleImageView.setOnClickListener {
            toggle()
        }

        binding.songPlayerShuffleImageView.setOnClickListener {
            shuffle()
        }

        binding.songPlayerRepeatImageView.setOnClickListener {
            repeat()
        }
    }

    private fun loadInitialData(aSong: ASong) {
        binding.songPlayerTitleTextView.text = aSong.title
        binding.songPlayerSingerNameTextView.text = aSong.artist
        binding.songPlayerTotalTimeTextView.text =
            formatTimeInMillisToString(aSong.length?.toLong() ?: 0L)

        if (aSong.clipArt.isNullOrEmpty()) binding.songPlayerImageView.setImageResource(R.drawable.placeholder)
        aSong.clipArt?.let {
            binding.songPlayerImageView.load(File(it)) {
                CachePolicy.ENABLED
            }
        }
    }

    companion object {

        private val TAG = PlayerActivity::class.java.name

        fun start(context: Context, song: Song, songList: ArrayList<Song>) {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra(ASong::class.java.name, song)
                putExtra(SONG_LIST_KEY, songList)
            }
            context.startActivity(intent)
        }
    }
}