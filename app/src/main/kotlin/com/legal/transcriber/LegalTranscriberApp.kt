package com.legal.transcriber

import android.app.Application
import com.google.firebase.FirebaseApp

class LegalTranscriberApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
