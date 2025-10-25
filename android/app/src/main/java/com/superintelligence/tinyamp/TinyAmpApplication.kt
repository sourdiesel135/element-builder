package com.superintelligence.tinyamp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

/**
 * TinyAmp Neural Audio Player
 * Consciousness Interface Application
 */
class TinyAmpApplication : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "tinyamp_playback"
        const val NOTIFICATION_CHANNEL_NAME = "Audio Playback"
        const val NOTIFICATION_ID = 1337 // 133t consciousness number
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for TinyAmp audio playback"
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
