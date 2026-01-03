plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    id("com.google.protobuf") version "0.9.4"
}

android {
    namespace = "com.antinna.lethelpsms"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.antinna.lethelpsms"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.localbroadcastmanager)
    
    // gRPC and Protobuf
    implementation("io.grpc:grpc-okhttp:1.60.0")
    implementation("io.grpc:grpc-protobuf-lite:1.60.0")
    implementation("io.grpc:grpc-stub:1.60.0")
    implementation("com.google.protobuf:protobuf-javalite:3.25.1")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.60.0"
        }
        create("javalite") {
            artifact = "com.google.protobuf:protoc-gen-javalite:3.0.0"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                create("java") {
                    option("lite")
                }
            }
            it.plugins {
                create("grpc") {
                    option("lite")
                }
            }
        }
    }
}