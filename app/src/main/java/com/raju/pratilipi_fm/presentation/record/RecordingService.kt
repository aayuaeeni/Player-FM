package com.raju.pratilipi_fm.presentation.record

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.raju.pratilipi_fm.utils.Utils.getDirectoryPath
import java.io.IOException
import java.util.*

class RecordingService : Service() {
    private val CLASS_NAME = javaClass.simpleName
    private var mFileName: String? = null
    private var mFilePath: String? = null
    private var mRecorder: MediaRecorder? = null
    private var mStartingTimeMillis: Long = 0
    private var mElapsedMillis: Long = 0
    private var mIncrementTimerTask: TimerTask? = null
    private val myBinder: IBinder = LocalBinder()
    var isRecording = false
    /**
     * The following code implements a bound Service used to connect this Service to an Activity.
     */
    inner class LocalBinder : Binder() {
        val service: RecordingService
            get() = this@RecordingService
    }

    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }

    /**
     * Interface used to communicate to a connected component changes in the status of a
     * recording:
     * - recording started
     * - recording stopped (with file path)
     * - seconds elapsed and max amplitude (useful for graphical effects)
     */
    interface OnRecordingStatusChangedListener {
        fun onRecordingStarted()
        fun onTimerChanged(seconds: Int)
        fun onAmplitudeInfo(amplitude: Int)
        fun onRecordingStopped(filePath: String?, elapsedMillis: Long?)
    }

    private var onRecordingStatusChangedListener: OnRecordingStatusChangedListener? = null
    fun setOnRecordingStatusChangedListener(onRecordingStatusChangedListener: OnRecordingStatusChangedListener?) {
        this.onRecordingStatusChangedListener = onRecordingStatusChangedListener
    }

    /**
     * The following code implements a started Service.
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        onStartCommandCalls++
        return START_NOT_STICKY
    }

    /**
     * The following code is shared by both started and bound Service.
     */
    override fun onCreate() {
        onCreateCalls++
        super.onCreate()
    }

    override fun onDestroy() {
        onDestroyCalls++
        super.onDestroy()
        if (mRecorder != null) {
            stopRecording()
        }
        if (onRecordingStatusChangedListener != null) onRecordingStatusChangedListener = null
    }

    fun startRecording(duration: Int) {
        setFileNameAndPath()
        mRecorder = MediaRecorder()
        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mRecorder!!.setOutputFile(mFilePath)
        mRecorder!!.setMaxDuration(duration) // set the max duration, after which the Service is stopped
        mRecorder!!.setAudioChannels(1)
        mRecorder!!.setAudioSamplingRate(44100)
        mRecorder!!.setAudioEncodingBitRate(192000)

        // Called only if a max duration has been set.
        mRecorder!!.setOnInfoListener { mediaRecorder: MediaRecorder?, what: Int, extra: Int ->
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                stopRecording()
            }
        }
        try {
            mRecorder!!.prepare()
            mRecorder!!.start()
            mStartingTimeMillis = System.currentTimeMillis()
            isRecording = true
            startTimer()
        } catch (e: IOException) {
            Log.e(TAG, "$CLASS_NAME - startRecording(): prepare() failed$e")
        }
        if (onRecordingStatusChangedListener != null) {
            onRecordingStatusChangedListener!!.onRecordingStarted()
        }
    }

    private fun setFileNameAndPath() {
        mFileName = "pratilipi_fm" + System.currentTimeMillis()
        mFilePath = getDirectoryPath(this) + "/" + mFileName
        Log.d(TAG, "mFilePath =  $mFilePath")
    }

    private fun startTimer() {
        val mTimer = Timer()

        // Increment seconds.
        mElapsedMillis = 0
        mIncrementTimerTask = object : TimerTask() {
            override fun run() {
                mElapsedMillis += 100
                if (onRecordingStatusChangedListener != null) {
                    onRecordingStatusChangedListener!!.onTimerChanged(mElapsedMillis.toInt() / 1000)
                }
                if (onRecordingStatusChangedListener != null && mRecorder != null) {
                    try {
                        onRecordingStatusChangedListener!!.onAmplitudeInfo(mRecorder!!.maxAmplitude)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 0, 100)
    }

    fun stopRecording() {
        mRecorder!!.stop()
        val mElapsedMillis = System.currentTimeMillis() - mStartingTimeMillis
        mRecorder!!.release()
        isRecording = false
        mRecorder = null

        // Communicate the file path to the connected Activity.
        if (onRecordingStatusChangedListener != null) {
            onRecordingStatusChangedListener!!.onRecordingStopped(mFilePath, mElapsedMillis)
        }

        // Stop timer.
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask!!.cancel()
            mIncrementTimerTask = null
        }
        if (onRecordingStatusChangedListener == null) stopSelf()
        stopForeground(true)
    }

    companion object {
        private const val TAG = "AUDIO_RECORDER_TAG"
        private const val EXTRA_ACTIVITY_STARTER = "com.raju.pratilipi_fm.EXTRA_ACTIVITY_STARTER"
        var onCreateCalls = 0
        var onDestroyCalls = 0
        var onStartCommandCalls = 0
        fun makeIntent(context: Context, activityStarter: Boolean): Intent {
            val intent = Intent(context.applicationContext, RecordingService::class.java)
            intent.putExtra(EXTRA_ACTIVITY_STARTER, activityStarter)
            return intent
        }
    }
}