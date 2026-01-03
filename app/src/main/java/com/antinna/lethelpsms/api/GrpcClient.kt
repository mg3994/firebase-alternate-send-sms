package com.antinna.lethelpsms.api

import android.content.Context
import com.antinna.lethelpsms.proto.SmsRelayServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit

object GrpcClient {
    private var channel: ManagedChannel? = null
    private var stub: SmsRelayServiceGrpc.SmsRelayServiceBlockingStub? = null
    
    // In production, this should be configurable
    private const val HOST = "10.0.2.2" // Emulator localhost
    private const val PORT = 50051

    @Synchronized
    fun getStub(): SmsRelayServiceGrpc.SmsRelayServiceBlockingStub {
        if (channel == null || channel!!.isShutdown || channel!!.isTerminated) {
            channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                .usePlaintext() // For development only
                .build()
        }
        
        if (stub == null) {
            stub = SmsRelayServiceGrpc.newBlockingStub(channel)
        }
        
        return stub!!
    }

    fun shutdown() {
        channel?.shutdown()?.awaitTermination(1, TimeUnit.SECONDS)
        channel = null
        stub = null
    }
}
