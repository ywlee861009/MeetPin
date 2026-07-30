plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.kero.meetpin.core.designsystem"
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // BaseViewModel이 ViewModel을 상속하고 StateFlow/Flow를 public API로 노출하므로
    // 이를 상속하는 feature 모듈의 컴파일 클래스패스에도 노출되어야 한다 → api
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.kotlinx.coroutines.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // LocalWindowSizeClass가 WindowSizeClass 타입을 public API로 노출하므로,
    // 이를 소비하는 feature 모듈의 컴파일 클래스패스에도 타입이 보여야 한다 → api
    api(libs.androidx.material3.window.sizeclass)
    // 라이브 도트 맥동 애니메이션(rememberInfiniteTransition)
    implementation(libs.androidx.compose.animation)

    // @Preview 렌더링(개발용)
    debugImplementation(libs.androidx.ui.tooling)
}
