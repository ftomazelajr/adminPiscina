package com.tomazela.adminpiscina

import android.app.Application
import com.google.firebase.FirebaseApp

class AdminPiscinaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
