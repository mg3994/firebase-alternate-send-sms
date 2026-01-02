package com.antinna.lethelpsms.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SmsSenderWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Logic to send SMS and handle failures will be added here.
        return Result.success()
    }
}