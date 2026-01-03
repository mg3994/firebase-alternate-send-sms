package com.antinna.lethelpsms.api

import android.util.Log

/**
 * A placeholder for the gRPC client to communicate with the backend.
 */
class BackendApiClient {

    fun registerDevice(fcmToken: String): Boolean {
        // TODO: Implement actual gRPC call to register the device
        Log.d(TAG, "Registering device with token: $fcmToken")
        return true // Simulate successful registration
    }

    fun notifySmsSuccess(smsId: String) {
        // TODO: Implement actual gRPC call to notify SMS success
        Log.d(TAG, "Notifying backend of successful SMS: $smsId")
    }

    fun notifySmsFailure(smsId: String) {
        // TODO: Implement actual gRPC call to notify SMS failure and let do also pass FCM token in backend so that we can avoid this device to send the same sms from this device
        Log.d(TAG, "Notifying backend of failed SMS: $smsId")
    }

    companion object {
        private const val TAG = "BackendApiClient"
    }
}