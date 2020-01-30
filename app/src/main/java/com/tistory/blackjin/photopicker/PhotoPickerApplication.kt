package com.tistory.blackjin.photopicker

import android.app.Application
import timber.log.Timber

class PhotoPickerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}