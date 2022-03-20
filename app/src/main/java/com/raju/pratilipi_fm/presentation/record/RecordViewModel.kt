package com.raju.pratilipi_fm.presentation.record

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.raju.pratilipi_fm.R
import com.raju.pratilipi_fm.data.model.Recording
import com.raju.pratilipi_fm.utils.SingleLiveEvent

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    val serviceConnected: ObservableBoolean = ObservableBoolean(false)
    val serviceRecording: ObservableBoolean = ObservableBoolean(false)
    val secondsElapsed: ObservableInt = ObservableInt(0)
    private val toastMsg: SingleLiveEvent<Int> = SingleLiveEvent()
    private val amplitudeLive: MutableLiveData<Int> = MutableLiveData<Int>()
    var timeRemaining: MutableLiveData<String> = MutableLiveData<String>()
    val showPlayBack: ObservableBoolean = ObservableBoolean(false)

    var saveRecordingLD: MutableLiveData<Recording> = MutableLiveData<Recording>()


    private var recordingService: RecordingService? = null
    var recording: Recording? = null

    fun connectService(intent: Intent?) {
        getApplication<Application>().startService(intent)
        getApplication<Application>().bindService(
            intent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun disconnectAndStopService(intent: Intent?) {
        if (!serviceConnected.get()) return
        getApplication<Application>().unbindService(serviceConnection)
        if (!serviceRecording.get()) getApplication<Application>().stopService(intent)
        recordingService!!.setOnRecordingStatusChangedListener(null)
        recordingService = null
        serviceConnected.set(false)
    }

    fun startRecording() {
        recordingService!!.startRecording(RECORDING_TIME_MILLS)
        serviceRecording.set(true)
    }

    fun stopRecording() {
        recordingService!!.stopRecording()
    }

    fun getToastMsg(): SingleLiveEvent<Int> {
        return toastMsg
    }

    fun getAmplitudeLive(): LiveData<Int> {
        return amplitudeLive
    }

    /**
     * Implementation of ServiceConnection interface.
     * The interaction with the Service is managed by this view model.
     */
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            recordingService = (iBinder as RecordingService.LocalBinder).service
            serviceConnected.set(true)
            recordingService!!.setOnRecordingStatusChangedListener(onRecordingStatusChangedListener)
            serviceRecording.set(recordingService!!.isRecording)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            if (recordingService != null) {
                recordingService!!.setOnRecordingStatusChangedListener(null)
                recordingService = null
            }
            serviceConnected.set(false)
        }
    }

    /**
     * Implementation of RecordingService.OnRecordingStatusChangedListener interface.
     * The Service uses this interface to communicate to the connected component that a
     * recording has started/stopped, and the seconds elapsed, so that the UI can be updated
     * accordingly.
     */
    private val onRecordingStatusChangedListener: RecordingService.OnRecordingStatusChangedListener =
        object : RecordingService.OnRecordingStatusChangedListener {
            override fun onRecordingStarted() {
                serviceRecording.set(true)
                toastMsg.postValue(R.string.toast_recording_start)
                timeRemaining.postValue(RECORDING_TIME.toString())
                showPlayBack.set(false)
            }

            override fun onRecordingStopped(filePath: String?, elapsedMillis: Long?) {
                serviceRecording.set(false)
                secondsElapsed.set(0)
                timeRemaining.postValue(getApplication<Application>().getString(R.string.ready))
                toastMsg.postValue(R.string.toast_recording_saved)

                // Save the recording data in the database.
                recording = Recording(filePath!!, elapsedMillis!!)
                showPlayBack.set(true)
                saveRecordingLD.postValue(recording)
            }

            // This method is called from a separate thread.
            override fun onTimerChanged(seconds: Int) {
                secondsElapsed.set(seconds)
                timeRemaining.postValue((RECORDING_TIME - seconds).toString())
            }

            override fun onAmplitudeInfo(amplitude: Int) {
                amplitudeLive.postValue(amplitude)
            }
        }

    companion object {
        private const val TAG = "AUDIO_RECORDER_TAG"
        private const val RECORDING_TIME = 60
        private const val RECORDING_TIME_MILLS = RECORDING_TIME * 1000
    }
}