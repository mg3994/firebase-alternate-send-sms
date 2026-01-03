package com.antinna.lethelpsms.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.antinna.lethelpsms.api.BackendApiClient

class RegisterDeviceWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val fcmToken = inputData.getString(KEY_FCM_TOKEN)
            ?: return Result.failure()

        val apiClient = BackendApiClient()
        val success = apiClient.registerDevice(fcmToken)

        return if (success) {
            Log.d(TAG, "Device registered successfully")
            Result.success()
        } else {
            Log.e(TAG, "Device registration failed")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "RegisterDeviceWorker"
        const val KEY_FCM_TOKEN = "fcm_token"
    }
}