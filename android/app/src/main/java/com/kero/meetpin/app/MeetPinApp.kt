package com.kero.meetpin.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MeetPinApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
