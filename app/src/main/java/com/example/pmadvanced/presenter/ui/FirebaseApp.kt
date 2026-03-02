package com.example.pmadvanced.presenter.ui

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class FirebaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}