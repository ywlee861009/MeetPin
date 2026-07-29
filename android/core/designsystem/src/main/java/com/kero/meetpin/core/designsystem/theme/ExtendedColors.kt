package com.kero.meetpin.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Material3 색 역할에 없는 MeetPin 고유 시맨틱 컬러.
 *
 * 대표적으로 "도착(arrived)"은 강조색(핀 레드)과 명확히 구분돼야 하는 상태색이라
 * ColorScheme 바깥에서 별도 토큰으로 제공한다.
 *
 * 컴포넌트에서는 `MeetPinTheme.extendedColors.arrived` 형태로 접근한다.
 */
@Immutable
data class MeetPinExtendedColors(
    /** 가장 진한 제목 텍스트 (onSurface보다 강함). */
    val ink: Color,
    /** 도착 상태 강조색. */
    val arrived: Color,
    /** 도착 강조색 위 텍스트/아이콘. */
    val onArrived: Color,
    /** 도착 상태 약한 배경(필/뱃지). */
    val arrivedContainer: Color,
    /** arrivedContainer 위 텍스트. */
    val onArrivedContainer: Color,
    /** 헤어라인 경계 (outline과 동일, 접근 편의용). */
    val border: Color,
    /** 초대코드 등 모노 데이터 블록 배경. */
    val codeSurface: Color,
)

val MeetPinLightExtendedColors = MeetPinExtendedColors(
    ink = InkLight,
    arrived = ArrivedGreenLight,
    onArrived = Color.White,
    arrivedContainer = ArrivedWeakLight,
    onArrivedContainer = ArrivedInkLight,
    border = BorderLight,
    codeSurface = SurfaceContainerLight,
)

val MeetPinDarkExtendedColors = MeetPinExtendedColors(
    ink = InkDark,
    arrived = ArrivedGreenDark,
    onArrived = Color(0xFF06231A),
    arrivedContainer = ArrivedWeakDark,
    onArrivedContainer = ArrivedInkDark,
    border = BorderDark,
    codeSurface = SurfaceContainerDark,
)

/**
 * 확장 컬러 주입 지점. [MeetPinTheme]가 값을 제공한다.
 * 기본값은 라이트 — Provider 없이 접근하면 라이트로 폴백한다.
 */
val LocalMeetPinExtendedColors = staticCompositionLocalOf { MeetPinLightExtendedColors }
