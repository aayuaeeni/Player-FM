package com.raju.pratilipi_fm.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun getDirectoryPath(context: Context): String {
        if (isExternalStorageWritable) {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS),
                "AudioRecorder"
            )
            if (file.mkdirs()) {
                return file.absolutePath
            }
        }
        return context.filesDir.absolutePath // use internal storage if external storage is not available
    }

    private val isExternalStorageWritable: Boolean
        private get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    // Format seconds elapsed for chronometer in mm:ss format.
    fun formatSecondsElapsedForChronometer(seconds: Int): String {
        val mTimerFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        return mTimerFormat.format(Date(seconds * 1000L))
    }
}