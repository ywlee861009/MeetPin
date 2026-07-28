plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.kero.meetpin.core.map.google"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:map"))
    implementation(project(":core:model"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    // GoogleMap 컴포저블 시그니처가 foundation-layout(PaddingValues)을 참조한다.
    implementation("androidx.compose.foundation:foundation")

    // Google Maps SDK — 벤더 의존성은 이 모듈에만 격리된다.
    implementation(libs.google.maps.compose)
    implementation(libs.play.services.maps)
}
