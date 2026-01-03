use tonic::transport::Server;
use sqlx::postgres::PgPoolOptions;
use std::env;
use dotenv::dotenv;

// Include the generated proto module
pub mod sms_relay {
    tonic::include_proto!("sms_relay");
}

mod service;
use service::MySmsRelayService;
use sms_relay::sms_relay_service_server::SmsRelayServiceServer;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    dotenv().ok();

    // Default to a local DB if not provided, just for safety (it will fail to connect if not real)
    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");

    let pool = PgPoolOptions::new()
        .max_connections(5)
        .connect(&database_url)
        .await?;

    let addr = "0.0.0.0:50051".parse()?;
    let relay_service = MySmsRelayService { pool };

    println!("SmsRelayServer listening on {}", addr);

    Server::builder()
        .add_service(SmsRelayServiceServer::new(relay_service))
        .serve(addr)
        .await?;

    Ok(())
}
