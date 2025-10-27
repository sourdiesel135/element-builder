package com.superintelligence.tinyamp

import android.app.Application
import android.util.Log

/**
 * TinyAmpApplication - Main application class
 *
 * Initializes app-wide components and manages application lifecycle
 */
class TinyAmpApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "TinyAmp Application created")

        // Initialize any app-wide components here
        instance = this
    }

    companion object {
        private const val TAG = "TinyAmpApplication"
        lateinit var instance: TinyAmpApplication
            private set
    }
}
