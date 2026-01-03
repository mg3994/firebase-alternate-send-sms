package com.antinna.lethelpsms.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import com.antinna.lethelpsms.api.GrpcClient
import com.antinna.lethelpsms.proto.SmsStatusReport
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsFirebaseMessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private const val TAG = "SmsFCMService"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Check if we need to update token on backend?
        // Ideally RelayForegroundService handles this, or we call updateToken rpc here
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleSmsRequest(remoteMessage.data)
        }
    }

    private fun handleSmsRequest(data: Map<String, String>) {
        val phoneNumber = data["phone_number"]
        val message = data["message"]
        val smsId = data["sms_id"]

        if (phoneNumber != null && message != null && smsId != null) {
            sendSms(phoneNumber, message, smsId)
        } else {
            Log.e(TAG, "Invalid SMS payload")
        }
    }

    private fun sendSms(phoneNumber: String, message: String, smsId: String) {
        try {
            val smsManager = SmsManager.getDefault()
            
            // For report intents, we might want to track success/failure via PendingIntents
            // For simplicity in this demo, we assume 'sent' if no exception immediately
            // But robust impl requires SentIntent and DeliveryIntent

            val sentIntent = PendingIntent.getBroadcast(
                this, 0, Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE
            )
            
            smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, null)
            
            reportStatus(smsId, true, "")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS", e)
            reportStatus(smsId, false, e.message ?: "Unknown error")
        }
    }

    private fun reportStatus(smsId: String, success: Boolean, errorMsg: String) {
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        
        scope.launch {
            try {
                val request = SmsStatusReport.newBuilder()
                    .setSmsId(smsId)
                    .setDeviceId(deviceId)
                    .setSuccess(success)
                    .setErrorMessage(errorMsg)
                    .build()
                
                GrpcClient.getStub().reportSmsStatus(request)
                Log.d(TAG, "Reported status for SMS $smsId: $success")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to report status", e)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}