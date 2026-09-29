plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val motoGpsApiBaseUrl = providers.gradleProperty("motogpsApiBaseUrl")
    .orElse(providers.environmentVariable("MOTOGPS_API_BASE_URL"))
    .orElse("")
    .get()
    .trim()
    .trimEnd('/')
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

android {
    namespace = "pt.motogps.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "pt.motogps.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "MOTOGPS_API_BASE_URL", "\"$motoGpsApiBaseUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.webkit:webkit:1.17.1")
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}
