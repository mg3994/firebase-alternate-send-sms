package com.antinna.lethelpsms.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.antinna.lethelpsms.R
import com.antinna.lethelpsms.api.GrpcClient
import com.antinna.lethelpsms.proto.DeviceRegistrationRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RelayForegroundService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private const val CHANNEL_ID = "RelayServiceChannel"
        private const val TAG = "RelayForegroundService"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, com.antinna.lethelpsms.MainActivity::class.java) // Assuming MainActivity exists or will exist
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SMS Relay Active")
            .setContentText("Listening for remote SMS tasks...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this resource exists, default android unused
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        registerDevice()

        return START_STICKY
    }

    private fun registerDevice() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            scope.launch {
                try {
                    val request = DeviceRegistrationRequest.newBuilder()
                        .setDeviceId(deviceId)
                        .setFcmToken(token)
                        .addCapabilities("SMS")
                        .build()

                    val response = GrpcClient.getStub().registerDevice(request)
                    Log.d(TAG, "Registration response: ${response.message}")
                } catch (e: Exception) {
                    Log.e(TAG, "Registration failed", e)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "SMS Relay Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}