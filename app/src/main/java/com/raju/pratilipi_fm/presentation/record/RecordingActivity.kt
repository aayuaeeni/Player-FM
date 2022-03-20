package com.raju.pratilipi_fm.presentation.record

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModelProvider
import com.raju.player.model.ASong
import com.raju.pratilipi_fm.R
import com.raju.pratilipi_fm.data.model.Recording
import com.raju.pratilipi_fm.data.model.Song
import com.raju.pratilipi_fm.databinding.ActivityRecordingBinding
import com.raju.pratilipi_fm.presentation.player.PlayerActivity
import com.raju.pratilipi_fm.presentation.playlist.PlaylistActivity
import com.raju.pratilipi_fm.presentation.playlist.PlaylistViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class RecordingActivity : AppCompatActivity() {
    lateinit var binding: ActivityRecordingBinding
    private var recordViewModel: RecordViewModel? = null

    private var firstCallback = true
    private var secsCallback: OnPropertyChangedCallback? = null
    private val viewModel: PlaylistViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recording)
        recordViewModel = ViewModelProvider(this).get(RecordViewModel::class.java)
        initActions()
        initObserevers()

    }

    private fun initActions() {
        binding.timeRemaining.setOnClickListener {
            Log.d("Raju","Raki")
        }
        binding.btnRecord.setOnClickListener {
            startStopRecording()
        }

        binding.btnPlay.setOnClickListener {
            if (recordViewModel?.recording == null){
                return@setOnClickListener
            }
            startPlaying(recordViewModel?.recording!!)
        }
    }

    override fun onResume() {
        super.onResume()
        firstCallback = true
        // When receiving the first second, adjust the line of times.
        secsCallback = object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                if (firstCallback) {
                    firstCallback = false
                    val secs = (sender as ObservableInt).get()
                    Objects.requireNonNull(this@RecordingActivity).runOnUiThread {
                        binding.bitView.startRecording(
                            secs
                        )
                    }
                }
            }
        }
        recordViewModel!!.secondsElapsed.addOnPropertyChangedCallback(secsCallback!!)
    }

    override fun onPause() {
        super.onPause()
        recordViewModel!!.secondsElapsed.removeOnPropertyChangedCallback(secsCallback!!)
    }

    private fun startPlaying(recording: Recording) {

        val id = getRandomInteger(1,1000)
        val song = Song(
            id,
            "Pratilipi",
            recording.path,
            "Pratilipi",
            "Pratilipi",
           recording.length.toString(),
            PlaylistActivity.AUDIO_TYPE
        )
        val songs : MutableList<ASong> = arrayListOf()
        songs.add(song)
        PlayerActivity.start(this, song, songs as ArrayList<Song>)
        finish()
    }

    private fun startStopRecording() {
        firstCallback = false
        if (!recordViewModel!!.serviceRecording.get()) { // start recording
            recordViewModel!!.startRecording()
            binding.bitView.startRecording(0)
        } else { //stop recording
            recordViewModel!!.stopRecording()
            binding.bitView.stopRecording()
        }
    }


    private fun initObserevers() {
        recordViewModel!!.getToastMsg().observe(this) { msgId ->
            Toast.makeText(
               this, getString(msgId!!), Toast.LENGTH_SHORT).show()
        }
        recordViewModel!!.getAmplitudeLive().observe(this) { integer ->
            binding.bitView.addAmplitude(
                integer.toFloat()
            )
        }

        recordViewModel!!.timeRemaining.observe(this) {
                time ->
            binding.timeRemaining.text = time
        }

        recordViewModel!!.serviceRecording.addOnPropertyChangedCallback(object :
            OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                val isRecording = (sender as ObservableBoolean).get()
            }
        })

        recordViewModel?.saveRecordingLD?.observe(this){
            val id = getRandomInteger(1,1000)
            val song = Song(
                id,
                "Pratilipi",
                it.path.toString(),
                "Pratilipi",
                "Pratilipi",
                it.length.toString(),
                PlaylistActivity.AUDIO_TYPE
            )
            viewModel.saveSongData(song)
        }
    }


    // Connection with local Service through the view model.
    override fun onStart() {
        super.onStart()
        recordViewModel?.connectService(RecordingService.makeIntent(this, true))
    }

    // Disconnection from local Service.
    override fun onStop() {
        super.onStop()
        recordViewModel?.disconnectAndStopService(Intent(this, RecordingService::class.java))
    }

    private fun getRandomInteger(maximum: Int, minimum: Int): Int {
        return (Math.random() * (maximum - minimum)).toInt() + minimum
    }

}