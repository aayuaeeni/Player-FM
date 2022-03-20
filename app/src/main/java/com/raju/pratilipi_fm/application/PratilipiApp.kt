package com.raju.pratilipi_fm.application

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.raju.pratilipi_fm.di.module.AppModule
import com.raju.pratilipi_fm.di.module.DatabaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PratilipiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)

        startKoin {
//            androidLogger()
            androidContext(this@PratilipiApp)
            modules(listOf(DatabaseModule, AppModule))
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}