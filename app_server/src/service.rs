use tonic::{Request, Response, Status};
use sqlx::PgPool;
use crate::sms_relay::sms_relay_service_server::SmsRelayService;
use crate::sms_relay::{
    DeviceRegistrationRequest, RegistrationResponse, TokenUpdateRequest, SmsStatusReport, Empty,
};

// Import the generated proto code. 
// Note: This module structure depends on how tonic generates code.
// Usually, with the build.rs setup, it's accessible via the package name.

pub struct MySmsRelayService {
    pub pool: PgPool,
}

#[tonic::async_trait]
impl SmsRelayService for MySmsRelayService {
    async fn register_device(
        &self,
        request: Request<DeviceRegistrationRequest>,
    ) -> Result<Response<RegistrationResponse>, Status> {
        let req = request.into_inner();
        println!("Received registration request for device: {}", req.device_id);

        // Logic: Upsert device info into DB
        // Assuming table 'devices' exists with columns: device_id, fcm_token, last_seen
        
        let result = sqlx::query(
            "INSERT INTO devices (device_id, fcm_token, last_seen) 
             VALUES ($1, $2, NOW())
             ON CONFLICT (device_id) 
             DO UPDATE SET fcm_token = $2, last_seen = NOW()"
        )
        .bind(&req.device_id)
        .bind(&req.fcm_token)
        .execute(&self.pool)
        .await;

        match result {
            Ok(_) => {
                Ok(Response::new(RegistrationResponse {
                    success: true,
                    message: "Device registered successfully".into(),
                }))
            },
            Err(e) => {
                eprintln!("Failed to register device: {}", e);
                // Return success=false locally or internal error if critical
                // For now, let's just log and return OK but with success=false message to client
                Ok(Response::new(RegistrationResponse {
                    success: false,
                    message: format!("Database error: {}", e),
                }))
            }
        }
    }

    async fn update_token(
        &self,
        request: Request<TokenUpdateRequest>,
    ) -> Result<Response<Empty>, Status> {
        let req = request.into_inner();
        println!("Updating token for device: {}", req.device_id);

        let _ = sqlx::query(
            "UPDATE devices SET fcm_token = $1, last_seen = NOW() WHERE device_id = $2"
        )
        .bind(&req.new_fcm_token)
        .bind(&req.device_id)
        .execute(&self.pool)
        .await
        .map_err(|e| Status::internal(e.to_string()))?;

        Ok(Response::new(Empty {}))
    }

    async fn report_sms_status(
        &self,
        request: Request<SmsStatusReport>,
    ) -> Result<Response<Empty>, Status> {
        let req = request.into_inner();
        println!("SMS Status Report: ID={}, Success={}", req.sms_id, req.success);

        // Logic: Update SMS status in 'sms_logs' or similar
        let status_str = if req.success { "DELIVERED" } else { "FAILED" };
        
        let _ = sqlx::query(
            "UPDATE sms_queue SET status = $1, error_message = $2, updated_at = NOW() WHERE id = $3"
        )
        .bind(status_str)
        .bind(if req.success { None } else { Some(req.error_message) })
        .bind(&req.sms_id) // Assuming sms_id is UUID string or similar
        .execute(&self.pool)
        .await
        .map_err(|e| {
            eprintln!("Failed to update SMS status: {}", e);
            Status::internal(e.to_string())
        })?;

        Ok(Response::new(Empty {}))
    }
}
