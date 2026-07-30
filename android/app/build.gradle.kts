import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)
}

/**
 * Google Maps API 키를 local.properties에서 읽는다. (local.properties는 VCS에 커밋하지 않음)
 * 키가 없으면 빌드를 막지 않고 경고만 남긴다 — CI와 신규 개발자 온보딩을 막지 않기 위함.
 * 키 발급 절차는 android/README.md 참고.
 */
val mapsApiKey: String = Properties().apply {
    rootProject.file("local.properties")
        .takeIf { it.exists() }
        ?.inputStream()
        ?.use { load(it) }
}.getProperty("MAPS_API_KEY").orEmpty().also {
    if (it.isBlank()) {
        logger.warn(
            "[MeetPin] local.properties에 MAPS_API_KEY가 없습니다. " +
                "지도가 회색 화면으로 표시됩니다. 설정 방법은 android/README.md 참고."
        )
    }
}

android {
    namespace = "com.kero.meetpin.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kero.meetpin"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // AndroidManifest.xml의 com.google.android.geo.API_KEY 치환값
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Core Modules
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:location"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:map"))
    // 지도 벤더 구현: 교체 시 이 줄과 앱 루트의 provider만 바꾼다.
    implementation(project(":core:map-google"))

    // Feature Modules
    implementation(project(":feature:map"))
    implementation(project(":feature:lobby"))
    implementation(project(":feature:tracking"))

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // AndroidX & Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // calculateWindowSizeClass(activity) 호출용 (사이즈 클래스는 :core:designsystem로 공급)
    implementation(libs.androidx.material3.window.sizeclass)

    // Location Services (지도 SDK는 :core:map-google 로 격리됨)
    implementation(libs.play.services.location)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
