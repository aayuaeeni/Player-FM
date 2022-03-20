package com.raju.pratilipi_fm.presentation.playlist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.raju.player.BasePlayerActivity
import com.raju.pratilipi_fm.R
import com.raju.pratilipi_fm.data.model.Song
import com.raju.pratilipi_fm.databinding.ActivityPlaylistBinding
import com.raju.pratilipi_fm.presentation.player.PlayerActivity
import com.raju.pratilipi_fm.presentation.record.RecordingActivity
import com.raju.pratilipi_fm.utils.OptionSheet
import org.koin.android.viewmodel.ext.android.viewModel

class PlaylistActivity : BasePlayerActivity(), OnPlaylistAdapterListener,OptionSheet.OptionSheetListener {

    lateinit var binding: ActivityPlaylistBinding

    private var adapter: PlaylistAdapter? = null
    private val viewModel: PlaylistViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PlaylistAdapter(this)
        binding.playlistRecyclerView.adapter = adapter


        binding.fab.setOnClickListener { view ->

            OptionSheet.listener = this@PlaylistActivity
            val optionSheet = OptionSheet()
            optionSheet.show(supportFragmentManager, optionSheet.tag)
        }

        viewModel.playlistData.observe(this, Observer {
            adapter?.songs = it
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.getSongsFromDb()
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_AUDIO_KEY) {
            data?.data?.let {
                addSong(it)
            }
        }
    }
    private fun addSong(musicData: Uri) {
        /*    val cursor = activity?.contentResolver?.query(musicData, null,null, null, null)*/
        val cursor = contentResolver?.query(
            musicData,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
            ), null, null, null
        )
        while (cursor?.moveToNext() == true) {
            val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
            val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
            val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
            val albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
            val duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

            val cursorAlbums = contentResolver?.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                MediaStore.Audio.Albums._ID + "=?",
                arrayOf<String>(albumId),
                null
            )
            var albumArt: String? = null
            if (cursorAlbums?.moveToFirst() == true) {
                albumArt =
                    cursorAlbums.getString(cursorAlbums.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
            }

            val song = Song(
                id.toInt(),
                title.toString(),
                path.toString(),
                artist,
                albumArt,
                duration,
                AUDIO_TYPE
            )
            viewModel.saveSongData(song)
        }
        cursor?.close()
    }

    private fun showRemoveSongItemConfirmDialog(song: Song) {
        // setup the alert builder
        AlertDialog.Builder(this)
            .setMessage("Are you sure to remove this song?")
            // add a button
            .apply {
                setPositiveButton(R.string.yes) { _, _ ->
                    removeMusicFromList(song)
                }
                setNegativeButton(R.string.no) { _, _ ->
                    // User cancelled the dialog
                }
            }
            // create and show the alert dialog
            .show()
    }

    override fun removeSongItem(song: Song) {
        showRemoveSongItemConfirmDialog(song)
    }

    private fun removeMusicFromList(song: Song) {
        songPlayerViewModel.stop()
        viewModel.removeItemFromList(song)
    }

    private fun openMusicList() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_AUDIO_KEY)
    }


    override fun playSong(song: Song, songs: ArrayList<Song>) {
        PlayerActivity.start(this, song, songs)
    }


    companion object {
        private val TAG = PlaylistActivity::class.java.name
        const val PICK_AUDIO_KEY = 2017
        const val AUDIO_TYPE = 3
    }

    override fun onOptionClicked(isRecord: Boolean?) {
        if (isRecord == true){
            val intent = Intent(this, RecordingActivity::class.java)
            startActivity(intent)
        }else{
            openMusicList()
        }
    }
}
