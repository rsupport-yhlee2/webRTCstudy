package com.example.webrtcstudy

import android.app.Application
import com.google.firebase.FirebaseApp

class WebRTCApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
    }
}