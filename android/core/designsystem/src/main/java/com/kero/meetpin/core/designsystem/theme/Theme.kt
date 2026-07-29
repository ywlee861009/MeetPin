package com.kero.meetpin.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * MeetPin 앱 테마.
 *
 * `dynamicColor`는 **지원하지 않는다**. 브랜드 색(핀 레드)이 앱의 정체성이므로
 * Android 12+ 의 사용자 배경화면 색(Material You)으로 덮이면 안 된다.
 *
 * Material3 [MaterialTheme] 위에 MeetPin 확장 컬러([MeetPinExtendedColors])를
 * CompositionLocal로 함께 제공한다. 확장 컬러는 [MeetPinTheme] 객체로 접근한다.
 *
 * ```
 * val arrived = MeetPinTheme.extendedColors.arrived
 * ```
 */
@Composable
fun MeetPinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) MeetPinDarkColorScheme else MeetPinLightColorScheme
    val extendedColors = if (darkTheme) MeetPinDarkExtendedColors else MeetPinLightExtendedColors

    CompositionLocalProvider(LocalMeetPinExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MeetPinTypography,
            shapes = MeetPinShapes,
            content = content,
        )
    }
}

/**
 * MeetPin 테마 접근자. Material3의 `MaterialTheme` 객체와 같은 패턴으로,
 * 확장 토큰을 정적 진입점으로 노출한다.
 */
object MeetPinTheme {
    val extendedColors: MeetPinExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMeetPinExtendedColors.current

    /** M3 colorScheme 재노출(편의). */
    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme
}
