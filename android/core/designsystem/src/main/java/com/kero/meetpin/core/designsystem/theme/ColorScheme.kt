package com.kero.meetpin.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 원시 팔레트를 Material3 색 역할(role)로 매핑한다.
 *
 * - primary = 브랜드 핀 레드 (강조색)
 * - secondary/tertiary = 쿨 슬레이트 계열 뉴트럴 (강조색을 두 개로 늘리지 않는다)
 * - onSurface = 본문 텍스트, onSurfaceVariant = 보조 텍스트
 *
 * M3에 없는 시맨틱(도착 등)은 [MeetPinExtendedColors]에서 별도로 제공한다.
 *
 * 주의: onPrimary는 두 테마 모두 흰색이다. 핀 레드 위 흰 볼드 텍스트는 대비 AA-large 구간이므로
 * 버튼 라벨은 항상 볼드/충분한 크기(labelLarge)로 쓴다.
 */
val MeetPinLightColorScheme = lightColorScheme(
    primary = MeetPinRed,
    onPrimary = Color.White,
    primaryContainer = MeetPinRedWeakLight,
    onPrimaryContainer = MeetPinRedInkLight,

    secondary = SlateLight,
    onSecondary = Color.White,
    secondaryContainer = SurfaceContainerLight,
    onSecondaryContainer = InkLight,

    tertiary = SlateLight,
    onTertiary = Color.White,
    tertiaryContainer = OutlineWeakLight,
    onTertiaryContainer = InkLight,

    background = GroundLight,
    onBackground = TextLight,
    surface = SurfaceLight,
    onSurface = TextLight,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = MutedLight,

    outline = BorderLight,
    outlineVariant = OutlineWeakLight,

    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorWeakLight,
    onErrorContainer = ErrorInkLight,
)

val MeetPinDarkColorScheme = darkColorScheme(
    primary = MeetPinRedBright,
    onPrimary = Color(0xFF2A0E10),
    primaryContainer = MeetPinRedWeakDark,
    onPrimaryContainer = MeetPinRedInkDark,

    secondary = SlateDark,
    onSecondary = Color(0xFF1B1E24),
    secondaryContainer = SurfaceContainerDark,
    onSecondaryContainer = InkDark,

    tertiary = SlateDark,
    onTertiary = Color(0xFF1B1E24),
    tertiaryContainer = OutlineWeakDark,
    onTertiaryContainer = InkDark,

    background = GroundDark,
    onBackground = TextDark,
    surface = SurfaceDark,
    onSurface = TextDark,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = MutedDark,

    outline = BorderDark,
    outlineVariant = OutlineWeakDark,

    error = ErrorDark,
    onError = Color(0xFF2A0E0C),
    errorContainer = ErrorWeakDark,
    onErrorContainer = ErrorInkDark,
)
